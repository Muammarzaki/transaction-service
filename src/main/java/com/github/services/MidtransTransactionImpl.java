package com.github.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.domain.MidtransDomain;
import com.github.domain.TransactionDomain;
import com.github.entities.*;
import com.github.helpers.*;
import com.github.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Service
public class MidtransTransactionImpl implements TransactionService {

	public static final String BASE_URL = "https://api.sandbox.midtrans.com/v2";
	private static final String INVOICE = "invoice";
	private final RestClient restClient;
	private final TransactionRepository repository;
	public final ObjectMapper objectMapper;

	public MidtransTransactionImpl(RestClient.Builder restClient, @Value("${midtrans.server-key}") String serverKey, TransactionRepository repository, ObjectMapper objectMapper) {
		this.repository = repository;
		this.objectMapper = objectMapper;

		final String AUTH_STRING = serverKey + ":";
		String credential = String.format("Basic %s", Base64.getEncoder().encodeToString(AUTH_STRING.getBytes(StandardCharsets.UTF_8)));
		this.restClient = restClient
			.baseUrl(BASE_URL)
			.defaultHeader(HttpHeaders.AUTHORIZATION, credential)
			.defaultHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
			.defaultHeader(HttpHeaders.ACCEPT, APPLICATION_JSON_VALUE)
			.build();
	}

	@Override
	@Transactional
	public TransactionDomain.Response createTransaction(TransactionDomain.CreateTransact dataCreate, ZoneId zoneId) {
		TransactionDomain.CustomerDomain customer = dataCreate.customer();

		MidtransDomain.PaymentMethod paymentType = MidtransDomain.PaymentMethod.fromSubType(dataCreate.transactMethod());

		MidtransDomain.TransactionRequest transactionRequest = MidtransDomain.TransactionRequest.builder()
			.paymentType(paymentType)
			.transactionDetails(new MidtransDomain.TransactionDetails(UUID.randomUUID().toString(), dataCreate.grossAmount()))
			.items(dataCreate.items())
			.customerDetails(new MidtransDomain.CustomerDetails(customer.username(), "example@gmail.com"))
			.build();

		Object anyData = switch (paymentType) {
			case CSTORE -> new MidtransDomain.Cstore(dataCreate.transactMethod(), "--");
			case BANK_TRANSFER -> new MidtransDomain.BankTransfer(dataCreate.transactMethod());
			case CREDIT_CARD, QRIS -> null;
		};
		if (anyData != null)
			transactionRequest.addAny(paymentType.getType(), anyData);

		MidtransDomain.TransactionResponse mResponse = restClient.post()
			.uri("/charge")
			.body(transactionRequest)
			.exchange((clientRequest, clientResponse) -> {
				String bodyResponse = clientResponse.bodyTo(String.class);
				HttpStatusCode statusCode = clientResponse.getStatusCode();
				if (statusCode.isSameCodeAs(HttpStatusCode.valueOf(429))) {
					throw new ThirdPartyRequestErrorException(statusCode, "API rate limit exceeded");
				}
				MidtransDomain.StatusResponse status = objectMapper.readValue(bodyResponse, MidtransDomain.StatusResponse.class);
				if (statusCode.is2xxSuccessful() && (status.getStatusCode() == 201)) {
					return objectMapper.readValue(bodyResponse, MidtransDomain.TransactionResponse.class);
				}
				throw new ThirdPartyRequestErrorException(HttpStatusCode.valueOf(status.getStatusCode()), status.getStatusMessage());
			});
		InvoiceEntity invoice = switch (mResponse) {
			case MidtransDomain.BankTransferResponse bank -> bank.getVaNumbers().stream()
				.findFirst()
				.map(vaNumber -> new BankTransferInvoiceEntity(vaNumber.vaNumber(), vaNumber.bank()))
				.orElseThrow(() -> new UnReceiveInvoice("UnReceive from midtrans with order id = %s".formatted(mResponse.getOrderId())));
			case MidtransDomain.CStoreResponse cstore ->
				new CStoreInvoiceEntity(cstore.getPaymentCode(), cstore.getStore());
			case MidtransDomain.EWalletResponse eWallet -> eWallet.getActions().stream()
				.findFirst()
				.map(action -> new EWalletInvoiceEntity(action.name(), action.method(), action.url()))
				.orElseThrow(() -> new UnReceiveInvoice("UnReceive from midtrans with order id = %s".formatted(mResponse.getOrderId())));
			default -> throw new IllegalStateException("Unexpected value: " + mResponse);
		};

		TransactionEntity entity = Optional.of(mResponse)
			.map(x -> TransactionEntity.builder()
				.transactStatus(x.getTransactionStatus())
				.transactOn(x.getTransactionTime().toInstant(ZoneOffset.UTC))
				.transactMethod(dataCreate.transactMethod())
				.transactId(x.getTransactionId())
				.grossAmount(x.getGrossAmount())
				.items(ItemAdapter.convertFromListOfItemEntityToItemEntity(dataCreate.items()))
				.customerInfo(CustomerAdapter.convertToCustomerInfo(dataCreate.customer()))
				.currency(String.valueOf(x.getCurrency()))
				.orderId(x.getOrderId())
				.invoice(invoice)
				.build())
			.orElseThrow(() -> new TransactionNotFoundException("midtrans does not have response body or empty"));

		repository.save(entity);

		return TransactionDomain.Response.builder()
			.orderId(entity.getOrderId())
			.transactStatus(entity.getTransactStatus())
			.grossAmount(entity.getGrossAmount())
			.currency(Currency.getInstance(entity.getCurrency()))
			.transactOn(LocalDateTime.ofInstant(entity.getTransactOn(), zoneId))
			.customer(CustomerAdapter.convertFromCustomerInfo(entity.getCustomerInfo()))
			.items(ItemAdapter.convertFromListOfItemEntityToItemsDomain(entity.getItems()))
			.transactMethod(entity.getTransactMethod())
			.anyProperty(Map.of(INVOICE, invoice))
			.build();
	}

	@Override
	public void cancelTransaction(String orderId) {
		MidtransDomain.TransactionResponse response = restClient.post()
			.uri("/{orderId}/cancel", orderId)
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
					.build();
				if (x.getTransactFinishOn() != null)
					response.setTransactFinishOn(LocalDateTime.ofInstant(x.getTransactFinishOn(), zoneId));
				return response;
			})
			.toList();
	}

	@Override
	public TransactionDomain.Response checkTransaction(String orderId, ZoneId zoneId) {
		return repository.findByOrderId(orderId).map(x -> {
				TransactionDomain.Response response = TransactionDomain.Response.builder()
					.transactStatus(x.getTransactStatus())
					.orderId(x.getOrderId())
					.transactOn(LocalDateTime.ofInstant(x.getTransactOn(), zoneId))
					.transactMethod(x.getTransactMethod())
					.grossAmount(x.getGrossAmount())
					.currency(Currency.getInstance(x.getCurrency()))
					.customer(CustomerAdapter.convertFromCustomerInfo(x.getCustomerInfo()))
					.anyProperty(Map.of(INVOICE, x.getInvoice()))
					.items(ItemAdapter.convertFromListOfItemEntityToItemsDomain(x.getItems()))
					.build();
				if (x.getTransactFinishOn() != null)
					response.setTransactFinishOn(LocalDateTime.ofInstant(x.getTransactFinishOn(), zoneId));
				return response;
			})
			.orElseThrow(() -> new TransactionNotFoundException("transaction with order_id %s not exits".formatted(orderId)));
	}

	public void updateTransaction(MidtransDomain.TransactionNotificationRequest request) {
		int affected;
		affected = repository.updateTransaction(request.getTransactionResponse().getOrderId(),
			request.getTransactionResponse().getTransactionStatus(),
			Instant.from(request.getTransactionResponse().getTransactionTime()));
		if (affected > 0) {
			return;
		}
		throw new TransactionNotFoundException("transaction with order %s not affected".formatted(request.getTransactionResponse().getOrderId()));
	}
}

