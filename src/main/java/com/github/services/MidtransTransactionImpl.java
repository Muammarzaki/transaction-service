package com.github.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.domain.MidtransDomain;
import com.github.domain.TransactionDomain;
import com.github.entites.TransactionEntity;
import com.github.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Service
public class MidtransTransactionImpl implements TransactionService {

	public static final String BASE_URL = "https://api.sandbox.midtrans.com/v2";
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
	public void createTransaction(TransactionDomain.CreateTransact dataCreate) {
		TransactionDomain.CustomerDomain customer = dataCreate.customer();

		MidtransDomain.PaymentMethod paymentType = MidtransDomain.PaymentMethod.fromSubType(dataCreate.transactMethod());

		MidtransDomain.TransactionRequest transactionRequest = MidtransDomain.TransactionRequest.builder()
			.paymentType(paymentType)
			.transactionDetails(new MidtransDomain.TransactionDetails(UUID.randomUUID().toString(), dataCreate.mount()))
			.items(dataCreate.items())
			.customerDetails(new MidtransDomain.CustomerDetails(customer.username(), "example@gmail.com"))
			.build();

		Object anyData = switch (paymentType) {
			case CSTORE -> new MidtransDomain.Cstore(dataCreate.transactMethod(), "--");
			case BANK_TRANSFER -> new MidtransDomain.BankTransfer(dataCreate.transactMethod());
			default -> throw new IllegalArgumentException("payment method not supported");
		};

		transactionRequest.addAny(paymentType.getType(), anyData);

		MidtransDomain.TransactionResponse mResponse = restClient.post()
			.uri("/charge")
			.body(transactionRequest)
			.exchange((clientRequest, clientResponse) -> {
				String bodyResponse = clientResponse.bodyTo(String.class);
				HttpStatusCode statusCode = clientResponse.getStatusCode();
				if (statusCode.isSameCodeAs(HttpStatusCode.valueOf(429))) {
					throw new HttpClientErrorException(statusCode, "API rate limit exceeded");
				}
				MidtransDomain.StatusResponse status = objectMapper.readValue(bodyResponse, MidtransDomain.StatusResponse.class);
				if (statusCode.is2xxSuccessful()) {
					if (status.getStatusCode() == 201)
						return objectMapper.readValue(bodyResponse, MidtransDomain.TransactionResponse.class);
				}
				throw new HttpClientErrorException(HttpStatusCode.valueOf(status.getStatusCode()), status.getStatusMessage());
			});

		TransactionEntity entity = Optional.of(mResponse)
			.map(x -> TransactionEntity.builder()
				.transactStatus(x.getTransactionStatus())
				.transactOn(x.getTransactionTime().toInstant(ZoneOffset.UTC))
				.transactMethod(dataCreate.transactMethod())
				.transactId(x.getTransactionId())
				.mount(x.getGrossAmount())
				.items(TransactionDomain.ItemsDomain.convertToItemEntity(dataCreate.items()))
				.customerInfo(TransactionDomain.CustomerDomain.convertToCustomerInfo(dataCreate.customer()))
				.currency(String.valueOf(x.getCurrency()))
				.orderId(x.getOrderId())
				.build())
			.orElseThrow(() -> new NoSuchElementException("midtrans does not have response body or empty"));

		repository.save(entity);
	}

	@Override
	public void cancelTransaction(String orderId) {
		ResponseEntity<Void> response = restClient.post()
			.uri("/{orderId}/cancel", orderId)
			.exchange((clientRequest, clientResponse) -> {
				HttpStatusCode statusCode = clientResponse.getStatusCode();
				String bodyResponse = clientResponse.bodyTo(String.class);
				if (statusCode.is2xxSuccessful()) {
					MidtransDomain.TransactionResponse transactionResponse = objectMapper.readValue(bodyResponse, MidtransDomain.TransactionResponse.class);

				}
				return null;
			});
	}

	@Override
	public List<TransactionDomain.Response> getAllTransaction() {
		return repository.findAll().stream().map(x -> TransactionDomain.Response.builder()
				.transactFinishOn(LocalDateTime.from(x.getTransactFinishOn()))
				.transactMethod(x.getTransactMethod())
				.transactOn(LocalDateTime.from(x.getTransactOn()))
				.transactStatus(x.getTransactStatus())
				.orderId(x.getOrderId())
				.build())
			.collect(Collectors.toList());
	}

	@Override
	public TransactionDomain.Response checkTransaction(String orderId) {
		return repository.findByOrderId(orderId).map(x -> TransactionDomain.Response.builder()
				.transactStatus(x.getTransactStatus())
				.orderId(x.getOrderId())
				.transactFinishOn(LocalDateTime.from(x.getTransactFinishOn()))
				.transactMethod(x.getTransactMethod()).transactOn(LocalDateTime.from(x.getTransactOn()))
				.mount(x.getMount())
				.currency(Currency.getInstance(x.getCurrency()))
				.customer(TransactionDomain.CustomerDomain.convertFromCustomerInfo(x.getCustomerInfo()))
				.items(TransactionDomain.ItemsDomain.convertFromListOfItemEntity(x.getItems()))
				.build())
			.orElseThrow(() -> new NoSuchElementException("transaction with order_id %s not exits"));
	}
}
