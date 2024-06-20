package com.github.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.domain.MidtransDomain;
import com.github.domain.TransactionDomain;
import com.github.entities.BankTransferInvoiceEntity;
import com.github.entities.CStoreInvoiceEntity;
import com.github.entities.EWalletInvoiceEntity;
import com.github.entities.TransactionEntity;
import com.github.helpers.*;
import com.github.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Service
@Slf4j
public class MidtransTransactionImpl implements TransactionService {
	public final String baseUrl;
	private static final String INVOICE = "invoice";
	private final RestClient restClient;
	private final TransactionRepository repository;
	public final ObjectMapper objectMapper;

	public MidtransTransactionImpl(RestClient.Builder restClient, @Value("${midtrans.server-key}") String serverKey, @Value("${midtrans.url}") String baseUrl, TransactionRepository repository, ObjectMapper objectMapper) {
		this.baseUrl = baseUrl;
		this.repository = repository;
		this.objectMapper = objectMapper;

		final String AUTH_STRING = serverKey + ":";
		String credential = String.format("Basic %s", Base64.getEncoder().encodeToString(AUTH_STRING.getBytes(StandardCharsets.UTF_8)));
		this.restClient = restClient
			.baseUrl(this.baseUrl)
			.defaultHeader(HttpHeaders.AUTHORIZATION, credential)
			.defaultHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
			.defaultHeader(HttpHeaders.ACCEPT, APPLICATION_JSON_VALUE)
			.build();
	}


	@Override
	@Transactional
	public TransactionDomain.Response createTransaction(TransactionDomain.CreateTransact transact, ZoneId zoneId) {
		TransactionDomain.CustomerDomain customer = transact.customer();
		MidtransDomain.PaymentMethod paymentType = MidtransDomain.PaymentMethod.fromSubType(transact.transactMethod());
		MidtransDomain.TransactionRequest transactionRequest = MidtransDomain.TransactionRequest.builder()
			.paymentType(paymentType)
			.transactionDetails(new MidtransDomain.TransactionDetails(
				UUID.randomUUID().toString(),
				transact.grossAmount()))
			.items(transact.items())
			.customerDetails(new MidtransDomain.CustomerDetails(customer.firstName(), customer.lastName(), customer.email(), customer.phone()))
			.build();

		Object anyData = switch (paymentType) {
			case CSTORE -> new MidtransDomain.Cstore(transact.transactMethod(), transact.message());
			case BANK_TRANSFER -> new MidtransDomain.BankTransfer(transact.transactMethod());
			default -> null;
		};
		if (anyData != null)
			transactionRequest.addAny(paymentType.getType(), anyData);

		MidtransDomain.TransactionResponse midtransApiResponse = restClient.post()
			.uri("/v2/charge")
			.body(transactionRequest)
			.retrieve()
			.onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
				InputStream body = response.getBody();
				String message = new BufferedReader(new InputStreamReader(body)).lines().collect(Collectors.joining("\n"));
				JsonNode bodyNode = objectMapper.readTree(message);
				throw new ThirdPartyRequestErrorException(response.getStatusCode(), bodyNode.has("status_message") ? bodyNode.get("status_message").asText() : "API rate limit exceeded");
			})
			.body(MidtransDomain.TransactionResponse.class);

		TransactionEntity entity = repository.save(Optional.ofNullable(midtransApiResponse)
			.map(x -> TransactionEntity.builder()
				.transactStatus(x.getTransactionStatus())
				.transactOn(x.getTransactionTime().toInstant(ZoneOffset.UTC))
				.transactMethod(transact.transactMethod())
				.transactId(x.getTransactionId())
				.grossAmount(x.getGrossAmount())
				.items(ItemAdapter.convertFromListOfItemEntityToItemEntity(transact.items()))
				.customerInfo(CustomerAdapter.convertToCustomerInfo(transact.customer()))
				.currency(String.valueOf(x.getCurrency()))
				.orderId(x.getOrderId())
				.invoice(switch (midtransApiResponse) {
					case MidtransDomain.BankTransferResponse bank -> bank.getVaNumbers().stream().findFirst()
						.map(vaNumber -> new BankTransferInvoiceEntity(vaNumber.vaNumber(), vaNumber.bank()))
						.orElseThrow(() -> new UnReceiveInvoice("UnReceive from midtrans with order id = %s".formatted(midtransApiResponse.getOrderId())));
					case MidtransDomain.CStoreResponse cstore ->
						new CStoreInvoiceEntity(cstore.getPaymentCode(), cstore.getStore());
					case MidtransDomain.EWalletResponse eWallet -> eWallet.getActions().stream()
						.findFirst()
						.map(action -> new EWalletInvoiceEntity(action.name(), action.method(), action.url()))
						.orElseThrow(() -> new UnReceiveInvoice("UnReceive from midtrans with order id = %s".formatted(midtransApiResponse.getOrderId())));
					default ->
						throw new IllegalStateException("Unexpected value: " + midtransApiResponse.getPaymentType().getType());
				})
				.message(transact.message())
				.build())
			.map(x -> {
				x.getInvoice().setExpired(midtransApiResponse.getExpiryTime().atZone(zoneId).toInstant());
				return x;
			})
			.orElseThrow(() -> new TransactionNotFoundException("midtrans does not have response body or empty")));


		return TransactionDomain.Response.builder()
			.orderId(entity.getOrderId())
			.transactStatus(entity.getTransactStatus())
			.grossAmount(entity.getGrossAmount())
			.currency(Currency.getInstance(entity.getCurrency()))
			.transactOn(LocalDateTime.ofInstant(entity.getTransactOn(), zoneId))
			.customer(CustomerAdapter.convertFromCustomerInfo(entity.getCustomerInfo()))
			.items(ItemAdapter.convertFromListOfItemEntityToItemsDomain(entity.getItems()))
			.transactMethod(entity.getTransactMethod())
			.anyProperty(Map.of(INVOICE, entity.getInvoice()))
			.message(transact.message())
			.build();
	}

	public TransactionDomain.Response cancelTransaction(String orderId, ZoneId zone) {
		TransactionEntity entity = repository.findByOrderId(orderId)
			.orElseThrow(() -> new TransactionNotFoundException("Transaction with order_id : %s never created or found".formatted(orderId)));

		MidtransDomain.TransactionResponse response = restClient.post()
			.uri("/v2/{orderId}/cancel", orderId)
			.exchange((clientRequest, clientResponse) -> {
				HttpStatusCode statusCode = clientResponse.getStatusCode();
				String bodyResponse = clientResponse.bodyTo(String.class);
				MidtransDomain.StatusResponse status = objectMapper.readValue(bodyResponse, MidtransDomain.StatusResponse.class);
				if (statusCode.is2xxSuccessful() && (status.getStatusCode() == 200)) {
					return objectMapper.readValue(bodyResponse, MidtransDomain.TransactionResponse.class);
				}
				throw new ThirdPartyRequestErrorException(HttpStatusCode.valueOf(status.getStatusCode()), status.getStatusMessage());
			});

		repository.updateTransaction(orderId, response.getTransactionStatus(), response.getTransactionTime().toInstant(ZoneOffset.UTC));
		return TransactionDomain.Response.builder()
			.orderId(entity.getOrderId())
			.transactStatus(response.getTransactionStatus())
			.grossAmount(entity.getGrossAmount())
			.currency(Currency.getInstance(entity.getCurrency()))
			.transactOn(LocalDateTime.ofInstant(entity.getTransactOn(), zone))
			.transactFinishOn(response.getTransactionTime())
			.customer(CustomerAdapter.convertFromCustomerInfo(entity.getCustomerInfo()))
			.items(ItemAdapter.convertFromListOfItemEntityToItemsDomain(entity.getItems()))
			.transactMethod(entity.getTransactMethod())
			.anyProperty(Map.of(INVOICE, entity.getInvoice()))
			.message(entity.getMessage())
			.build();
	}

	@Override
	public List<TransactionDomain.Response> getAllTransaction(ZoneId zoneId) {
		return repository.findAll().stream().map(x -> {
				TransactionDomain.Response response = TransactionDomain.Response.builder()
					.transactMethod(x.getTransactMethod())
					.transactOn(LocalDateTime.ofInstant(x.getTransactOn(), zoneId))
					.transactStatus(x.getTransactStatus())
					.grossAmount(x.getGrossAmount())
					.currency(Currency.getInstance(x.getCurrency()))
					.orderId(x.getOrderId())
					.items(ItemAdapter.convertFromListOfItemEntityToItemsDomain(x.getItems()))
					.customer(CustomerAdapter.convertFromCustomerInfo(x.getCustomerInfo()))
					.anyProperty(Map.of(INVOICE, x.getInvoice()))
					.message(x.getMessage())
					.build();
				if (x.getTransactFinishOn() != null)
					response.setTransactFinishOn(LocalDateTime.ofInstant(x.getTransactFinishOn(), zoneId));
				return response;
			})
			.toList();
	}

	@Override
	public TransactionDomain.Response findTransaction(String orderId, ZoneId zone) {
		return repository.findByOrderId(orderId).map(x -> {
				TransactionDomain.Response response = TransactionDomain.Response.builder()
					.transactStatus(x.getTransactStatus())
					.orderId(x.getOrderId())
					.transactOn(LocalDateTime.ofInstant(x.getTransactOn(), zone))
					.transactMethod(x.getTransactMethod())
					.grossAmount(x.getGrossAmount())
					.currency(Currency.getInstance(x.getCurrency()))
					.customer(CustomerAdapter.convertFromCustomerInfo(x.getCustomerInfo()))
					.anyProperty(Map.of(INVOICE, x.getInvoice()))
					.items(ItemAdapter.convertFromListOfItemEntityToItemsDomain(x.getItems()))
					.message(x.getMessage())
					.build();
				if (x.getTransactFinishOn() != null)
					response.setTransactFinishOn(LocalDateTime.ofInstant(x.getTransactFinishOn(), zone));
				return response;
			})
			.orElseThrow(() -> new TransactionNotFoundException("transaction with order_id %s not exits".formatted(orderId)));
	}

	public void updateTransaction(String orderId, String status, String transactTime) {
		int affected = repository.updateTransaction(orderId, status, Instant.parse(transactTime));
		if (affected < 1)
			throw new TransactionNotFoundException("transaction with order %s not affected".formatted(orderId));
		log.debug("Transaction with order_id {} successfully update by notification", orderId);
	}
}

