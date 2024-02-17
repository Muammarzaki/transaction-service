package com.github.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.domain.TransactionDomain;
import com.github.entites.TransactionEntity;
import com.github.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withTooManyRequests;

@Tag("integrity-testing")
@RestClientTest(MidtransTransactionImpl.class)
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {"midtrans.server-key=foobar"})
class MidtransTransactionImplTest {
	@Autowired
	MockRestServiceServer server;
	@MockBean
	TransactionRepository repository;
	@Autowired
	ObjectMapper mapper;

	@Autowired
	MidtransTransactionImpl midtransTransaction;

	private String alfamartTransactionRequestBody;

	@BeforeEach
	void setUp() {
		alfamartTransactionRequestBody = """
			{
			  "mount": 4840.00,
			  "currency": "IDR",
			  "transact_method": "alfamart",
			  "items": [
			    {
			      "item_id": "random",
			      "item_name": "megicom",
			      "count": 2,
			      "price": 24200
			    }
			  ],
			  "customer": {
			    "user_id": "random",
			    "username": "joni"
			  }
			}""";

	}

	@Test
	void shouldCreateNewTransaction() {
		String alfamartSuccessResponse = """
			{
			  "status_code": "201",
			  "status_message": "Success, cstore transaction is successful",
			  "transaction_id": "d615df87-c96f-4f5c-9d35-2d740d54c1a9",
			  "order_id": "order-101o-1578557780",
			  "merchant_id": "G812785002",
			  "gross_amount": "4840.00",
			  "currency": "IDR",
			  "payment_type": "cstore",
			  "transaction_time": "2020-01-09 15:16:19",
			  "transaction_status": "pending",
			  "fraud_status": "accept",
			  "payment_code": "8127740588870520",
			  "store": "alfamart"
			}""";
		assertDoesNotThrow(() -> {
			TransactionDomain.CreateTransact createTransact = mapper.readValue(alfamartTransactionRequestBody, TransactionDomain.CreateTransact.class);

			server.expect(requestTo("https://api.sandbox.midtrans.com/v2/charge"))
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andRespond(withSuccess(alfamartSuccessResponse, MediaType.APPLICATION_JSON));

			midtransTransaction.createTransaction(createTransact);

			ArgumentCaptor<TransactionEntity> captor = ArgumentCaptor.forClass(TransactionEntity.class);
			verify(repository, times(1)).save(captor.capture());

			TransactionEntity captorValue = captor.getValue();

			assertThat(captorValue.getOrderId()).isNotEmpty();
			assertThat(captorValue.getMount()).isEqualTo(createTransact.mount());

		});
	}

	@Test
	void shouldCantCreateNewTransaction() {
		String alfamartFailButSuccessResponse = """
			{
			  "status_code": "406",
			  "status_message": "The request could not be completed due to a conflict with the current state of the target resource, please try again",
			  "id": "2e7d71d2-8f72-4c31-91a5-36731edd0b71"
			}""";

		HttpClientErrorException httpClientErrorException = assertThrows(HttpClientErrorException.class, () -> {
			TransactionDomain.CreateTransact createTransact = mapper.readValue(alfamartTransactionRequestBody, TransactionDomain.CreateTransact.class);

			server.expect(requestTo("https://api.sandbox.midtrans.com/v2/charge"))
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andRespond(withSuccess(alfamartFailButSuccessResponse, MediaType.APPLICATION_JSON));

			midtransTransaction.createTransaction(createTransact);
			verify(repository, times(0)).save(any());
		});
		assertThat(httpClientErrorException.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(406));
		assertThat(httpClientErrorException.getStatusText())
			.isEqualTo("The request could not be completed due to a conflict with the current state of the target resource, please try again");
	}

	@Test
	void shouldCantCreateNewTransactionCauseRequestLimit() {
		HttpClientErrorException httpClientErrorException = assertThrows(HttpClientErrorException.class, () -> {
			TransactionDomain.CreateTransact createTransact = mapper.readValue(alfamartTransactionRequestBody, TransactionDomain.CreateTransact.class);

			server.expect(requestTo("https://api.sandbox.midtrans.com/v2/charge"))
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andRespond(withTooManyRequests());

			midtransTransaction.createTransaction(createTransact);
			verify(repository, times(0)).save(any());
		});
		assertThat(httpClientErrorException.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(429));
		assertThat(httpClientErrorException.getStatusText())
			.isEqualTo("API rate limit exceeded");
	}

	@Test
	void shouldCancelTheTransactionByOrderId() {

	}
}