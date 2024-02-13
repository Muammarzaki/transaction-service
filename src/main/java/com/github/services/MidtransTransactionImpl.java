package com.github.services;

import com.github.domain.MidtransDomain;
import com.github.domain.TransactionDomain;
import com.github.entites.CustomerInfoEntity;
import com.github.entites.ItemEntity;
import com.github.entites.TransactionEntity;
import com.github.repository.TransactionRepository;
import com.sun.nio.sctp.IllegalReceiveException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MidtransTransactionImpl implements TransactionService {

	private final RestClient restClient;
	private final TransactionRepository repository;

	public MidtransTransactionImpl(RestClient.Builder restClient, String serverKey, TransactionRepository repository) {
		this.repository = repository;

		final String AUTH_STRING = String.format("%s:", serverKey);
		String credential = String.format("Basic %s", Base64.getEncoder().encodeToString(AUTH_STRING.getBytes(StandardCharsets.UTF_8)));
		this.restClient = restClient
			.baseUrl("https://api.sandbox.midtrans.com/v2")
			.defaultHeader("Authorization", credential)
			.defaultHeader("Content-Type", "application/json")
			.defaultHeader("Accept", "application/json")
			.build();
	}

	@Override
	@Transactional
	public void createTransaction(TransactionDomain.CreateTransact dataCreate) {
		TransactionDomain.CustomerDomain customer = dataCreate.customer();

		MidtransDomain.PaymentMethod paymentType = MidtransDomain.PaymentMethod.fromSubType(dataCreate.transactMethod());
		MidtransDomain.TransactionRequest transactionRequest = MidtransDomain.TransactionRequest.builder()
			.paymentType(paymentType)
			.items(dataCreate.items())
			.customerDetails(new MidtransDomain.CustomerDetails(customer.username(), "example@gmail.com"))
			.transactionDetails(new MidtransDomain.TransactionDetails(UUID.randomUUID().toString(), dataCreate.mount()))
			.build();

		Object anyData = switch (paymentType) {
			case CSTORE -> new MidtransDomain.Cstore(dataCreate.transactMethod(), "");
			case BANK_TRANSFER -> new MidtransDomain.BankTransfer(dataCreate.transactMethod());
			default -> throw new IllegalArgumentException("payment method not supported");
		};

		transactionRequest.addAny(paymentType.getType(), anyData);

		ResponseEntity<MidtransDomain.TransactionResponse> response = restClient.post().uri("/charge")
			.body(transactionRequest)
			.retrieve().toEntity(MidtransDomain.TransactionResponse.class);
		MidtransDomain.TransactionResponse responseBody = Optional.ofNullable(response.getBody()).orElseThrow(() -> new IllegalReceiveException("response body are empty"));

		TransactionEntity.builder()
			.transact_id(responseBody.getTransactionId())
			.customerInfo(CustomerInfoEntity.builder().username(customer.username()).userId(customer.userId()).build())
			.mount(dataCreate.mount())
			.orderId(responseBody.getOrderId())
			.transactMethod(dataCreate.transactMethod())
			.transactOn(Instant.parse(responseBody.getTransactionTime()))
			.currency(String.valueOf(responseBody.getCurrency()))
			.items(dataCreate.items().stream().map(itm -> ItemEntity.builder()
					.ItemId(itm.itemId())
					.count(itm.count())
					.price(itm.price())
					.build())
				.collect(Collectors.toList()));
	}

	@Override
	public void removeTransaction(String transactId) {

	}

	@Override
	public List<TransactionDomain> getAllTransaction() {
		return null;
	}

	@Override
	public TransactionDomain checkTransaction(String transactId) {
		return null;
	}
}
