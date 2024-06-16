package com.github.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.domain.TransactionDomain;
import com.github.entities.*;
import com.github.helpers.ThirdPartyRequestErrorException;
import com.github.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
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

import java.time.*;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
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
	private TransactionEntity transactionData;

	@BeforeEach
	void setUp() {
		alfamartTransactionRequestBody = """
			{
			  "gross_amount": 4840.00,
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
		CustomerInfoEntity customerInfo = CustomerInfoEntity.builder()
			.userId("2434")
			.username("joko")
			.build();
		ItemEntity item1 = ItemEntity.builder().itemId("3242")
			.itemName("foobar")
			.price(10000)
			.quantity(2)
			.build();
		transactionData = TransactionEntity.builder()
			.transactId("2323")
			.grossAmount(20000)
			.orderId("order-1")
			.currency("IDR")
			.transactStatus("pending")
			.transactOn(Instant.ofEpochMilli(1706617931617L))
			.transactMethod("alfamart")
			.customerInfo(customerInfo)
			.invoice(new CStoreInvoiceEntity("123456789", "alfamart"))
			.items(List.of(
				item1
			))
			.build();

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

			midtransTransaction.createTransaction(createTransact, TimeZone.getDefault().toZoneId());

			ArgumentCaptor<TransactionEntity> captor = ArgumentCaptor.forClass(TransactionEntity.class);
			verify(repository, times(1)).save(captor.capture());

			TransactionEntity captorValue = captor.getValue();

			assertThat(captorValue.getOrderId()).isNotEmpty();
			assertThat(captorValue.getGrossAmount()).isEqualTo(createTransact.grossAmount());

		});
	}

	@Test
	void shouldCreateNewTransactionWithBankMethod() {
		String bankRequestBody = """
			{
			  "gross_amount": 4840.00,
			  "currency": "IDR",
			  "transact_method": "bri",
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
		String bankTransactionResponse = """
			{
			  "status_code": "201",
			  "status_message": "Success, Bank Transfer transaction is created",
			  "transaction_id": "9aed5972-5b6a-401e-894b-a32c91ed1a3a",
			  "order_id": "1466323342",
			  "gross_amount": "4840.0",
			  "payment_type": "bank_transfer",
			  "transaction_time": "2016-06-19 15:02:22",
			  "transaction_status": "pending",
			  "va_numbers": [
			    {
			      "bank": "bri",
			      "va_number": "8578000000111111"
			    }
			  ],
			  "fraud_status": "accept",
			  "currency": "IDR"
			}""";
		assertDoesNotThrow(() -> {
			TransactionDomain.CreateTransact createTransact = mapper.readValue(bankRequestBody, TransactionDomain.CreateTransact.class);

			server.expect(requestTo("https://api.sandbox.midtrans.com/v2/charge"))
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andRespond(withSuccess(bankTransactionResponse, MediaType.APPLICATION_JSON));

			midtransTransaction.createTransaction(createTransact, TimeZone.getDefault().toZoneId());

			ArgumentCaptor<TransactionEntity> captor = ArgumentCaptor.forClass(TransactionEntity.class);
			verify(repository, times(1)).save(captor.capture());

			TransactionEntity captorValue = captor.getValue();

			assertThat(captorValue.getOrderId()).isNotEmpty();
			assertThat(captorValue.getGrossAmount()).isEqualTo(createTransact.grossAmount());

		});
	}

	@Test
	void shouldNotCreateNewTransactionWithUndefinedMethodPayment() {
		String bankRequestBody = """
			{
			  "gross_amount": 4840.00,
			  "currency": "IDR",
			  "transact_method": "cash",
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

		IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> {
			TransactionDomain.CreateTransact createTransact = mapper.readValue(bankRequestBody, TransactionDomain.CreateTransact.class);
			midtransTransaction.createTransaction(createTransact, TimeZone.getDefault().toZoneId());
		});
		assertThat(illegalArgumentException.getMessage()).isEqualTo("Unknown payment method from sub-type: cash");

	}

	@Test
	void shouldCantCreateNewTransaction() {
		String alfamartFailButSuccessResponse = """
			{
			  "status_code": "406",
			  "status_message": "The request could not be completed due to a conflict with the current state of the target resource, please try again",
			  "id": "2e7d71d2-8f72-4c31-91a5-36731edd0b71"
			}""";

		HttpClientErrorException httpClientErrorException = assertThrows(ThirdPartyRequestErrorException.class, () -> {
			TransactionDomain.CreateTransact createTransact = mapper.readValue(alfamartTransactionRequestBody, TransactionDomain.CreateTransact.class);

			server.expect(requestTo("https://api.sandbox.midtrans.com/v2/charge"))
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andRespond(withSuccess(alfamartFailButSuccessResponse, MediaType.APPLICATION_JSON));

			midtransTransaction.createTransaction(createTransact, TimeZone.getDefault().toZoneId());
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

			midtransTransaction.createTransaction(createTransact, TimeZone.getDefault().toZoneId());
			verify(repository, times(0)).save(any());
		});
		assertThat(httpClientErrorException.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(429));
		assertThat(httpClientErrorException.getStatusText())
			.isEqualTo("API rate limit exceeded");
	}

	@Test
	void shouldCancelTheTransactionByOrderId() {
		String cancelSuccessResponse = """
			{
			  "status_code" : "200",
			  "status_message" : "Success, transaction is canceled",
			  "transaction_id" : "249fc620-6017-4540-af7c-5a1c25788f46",
			  "masked_card" : "48111111-1114",
			  "order_id" : "order-3",
			  "payment_type" : "bank_transfer",
			  "transaction_time" : "2015-02-26 14:39:33",
			  "transaction_status" : "cancel",
			  "fraud_status" : "accept",
			  "bank" : "bni",
			  "gross_amount" : "30000.00"
			}""";
		String orderId = "order-3";
		server.expect(requestTo("https://api.sandbox.midtrans.com/v2/" + orderId + "/cancel"))
			.andRespond(withSuccess(cancelSuccessResponse, MediaType.APPLICATION_JSON));

		assertDoesNotThrow(() -> {
			midtransTransaction.cancelTransaction(orderId);

			LocalDateTime transactTime = LocalDateTime.of(2015, 2, 26, 14, 39, 33);
			verify(repository, times(1)).updateTransaction(orderId, "cancel", transactTime.toInstant(ZoneOffset.UTC));
		});
	}

	@Test
	void testCancelTheTransactionButItFailCauseMerchantDeclineTheRequest() {
		String cancelFailResponse = """
			{
			  "status_code" : "412",
			  "status_message" : "Merchant cannot modify the status of the transaction"
			}""";
		String orderId = "order-3";
		server.expect(requestTo("https://api.sandbox.midtrans.com/v2/" + orderId + "/cancel"))
			.andRespond(withSuccess(cancelFailResponse, MediaType.APPLICATION_JSON));

		HttpClientErrorException exceptions = assertThrows(HttpClientErrorException.class, () -> {
			midtransTransaction.cancelTransaction(orderId);
			verify(repository, times(0)).updateTransaction(any(), any(), any());
		});
		assertThat(exceptions)
			.extracting("statusCode", "statusText")
			.contains(HttpStatusCode.valueOf(412), "Merchant cannot modify the status of the transaction");
	}

	@Test
	void testFindAllTransactionWithOneEntity() {
		when(repository.findAll()).thenReturn(List.of(transactionData));

		List<TransactionDomain.Response> allTransaction = midtransTransaction.getAllTransaction(TimeZone.getDefault().toZoneId());

		verify(repository, times(1)).findAll();

		assertThat(allTransaction).hasSize(1)
			.extracting("orderId", "grossAmount")
			.contains(tuple(transactionData.getOrderId(), transactionData.getGrossAmount()));
	}

	@Test
	void testFindAllTransactionWithOneEntityAndTransactFinishOnExits() {
		Instant transactFinishOn = transactionData.getTransactOn().plus(Period.ofDays(5));
		transactionData.setTransactFinishOn(transactFinishOn);
		when(repository.findAll()).thenReturn(List.of(transactionData));

		List<TransactionDomain.Response> allTransaction = midtransTransaction.getAllTransaction(TimeZone.getDefault().toZoneId());

		verify(repository, times(1)).findAll();

		assertThat(allTransaction).hasSize(1)
			.extracting("orderId", "grossAmount", "transactFinishOn")
			.contains(tuple(transactionData.getOrderId(), transactionData.getGrossAmount(), transactFinishOn.atZone(ZoneId.systemDefault()).toLocalDateTime()));
	}

	@Test
	void testCheckTheTransactionWithTransactionFinishOnNotExits() {
		when(repository.findByOrderId(anyString())).thenAnswer(new FindTransaction());

		TransactionDomain.Response entityResponse = midtransTransaction.checkTransaction("order-1", ZoneId.systemDefault());

		verify(repository, times(1)).findByOrderId("order-1");
		assertThat(entityResponse)
			.extracting("orderId", "customer.userId", "grossAmount")
			.contains("order-1", "2434", 20000.0);
	}

	@Test
	void testCheckTheTransactionWithTransactionFinishOnExits() {
		when(repository.findByOrderId(anyString())).thenAnswer(new FindTransaction());

		TransactionDomain.Response entityResponse = midtransTransaction.checkTransaction("order-4", ZoneId.systemDefault());

		verify(repository, times(1)).findByOrderId("order-4");
		assertThat(entityResponse)
			.extracting("orderId", "customer.userId", "grossAmount", "transactFinishOn")
			.contains("order-4", "2434", 20000.0, entityResponse.getTransactOn().plus(Period.ofDays(5)).atZone(ZoneId.systemDefault()).toLocalDateTime());
	}

	@Test
	void testCheckTheTransactionButNotExits() {
		when(repository.findByOrderId(anyString())).thenAnswer(new FindTransaction());
		NoSuchElementException noSuchElementException = assertThrows(NoSuchElementException.class, () -> {
			TransactionDomain.Response entityResponse = midtransTransaction.checkTransaction("order-2", ZoneId.systemDefault());
		});
		assertThat(noSuchElementException.getMessage()).isEqualTo("transaction with order_id order-2 not exits");
		verify(repository, times(1)).findByOrderId("order-2");
	}

	public static class FindTransaction implements Answer<Optional<TransactionEntity>> {
		@Override
		public Optional<TransactionEntity> answer(InvocationOnMock invocationOnMock) throws Throwable {
			CustomerInfoEntity customerInfo = CustomerInfoEntity.builder()
				.userId("2434")
				.username("joko")
				.build();
			ItemEntity item1 = ItemEntity.builder().itemId("3242")

				.itemName("foobar")
				.price(10000)
				.quantity(2)
				.build();
			Instant transactOn = LocalDateTime.of(2023, 2, 2, 2, 2, 2).toInstant(ZoneOffset.UTC);
			TransactionEntity transactionData = TransactionEntity.builder()
				.transactId("2323")
				.grossAmount(20000)
				.orderId("order-1")
				.currency("IDR")
				.transactStatus("pending")
				.transactOn(transactOn)
				.transactMethod("bri")
				.customerInfo(customerInfo)
				.invoice(new BankTransferInvoiceEntity("123456789", "bri"))
				.items(List.of(
					item1
				))
				.build();
			Object argument = invocationOnMock.getArgument(0);
			if (argument.equals("order-4")) {
				transactionData.setOrderId("order-4");
				transactionData.setTransactFinishOn(transactOn.plus(Period.ofDays(5)));
			}
			if (argument.equals(transactionData.getOrderId()))
				return Optional.of(transactionData);

			return Optional.empty();
		}
	}
}