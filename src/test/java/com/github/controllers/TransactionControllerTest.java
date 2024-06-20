package com.github.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.domain.TransactionDomain;
import com.github.helpers.TransactionNotFoundException;
import com.github.services.TransactionService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
@ActiveProfiles("test")
@Tag("integration-testing")
@AutoConfigureMockMvc
class TransactionControllerTest {
	@Autowired
	MockMvc mockMvc;
	@MockBean
	TransactionService service;
	@Autowired
	ObjectMapper mapper;
	static LocalDateTime fixTime = LocalDateTime.of(2021, 4, 4, 0, 0);

	@Test
	void testRequestCreateNewTransaction() {
		assertDoesNotThrow(() -> {
			TransactionDomain.CreateTransact createTransact = mapper.readValue("""
				{
				  "gross_amount": 40000.0,
				  "currency": "IDR",
				  "transact_method": "gopay",
				  "items": [
				    {
				      "item_id": "random",
				      "item_name": "comedic",
				      "quantity": 2,
				      "price": 20000.0
				    }
				  ],
				  "customer": {
				    "user_id": "random",
				    "first_name": "joni",
				    "last_name": "",
				    "email" : "cp@example.com",
				    "phone" : "62341243212"
				  },
				  "message" : "hello"
				}""", TransactionDomain.CreateTransact.class);
			when(service.createTransaction(any(TransactionDomain.CreateTransact.class), any(ZoneId.class)))
				.thenAnswer(new TransactResponseAnswer());

			mockMvc.perform(post("/transact/create")
					.content(mapper.writeValueAsBytes(createTransact))
					.contentType(MediaType.APPLICATION_JSON)
				)
				.andExpectAll(status().isCreated(),
					content().contentType(MediaType.APPLICATION_JSON),
					jsonPath("$.status").value("transaction successfully created"),
					jsonPath("$.data.customer.last_name").value(""),
					jsonPath("$.data.order_id").value("order-1"),
					jsonPath("$.data.transact_on").value("04-04-2021 00:00:00"),
					jsonPath("$.data.transact_method").value("gopay"))
			;

		});
	}


	@Test
	void testRequestCreateNewTransactionButErrorValidation() {
		assertDoesNotThrow(() -> {
			TransactionDomain.CreateTransact createTransact = mapper.readValue("""
				{
				  "gross_amount": -0.0,
				  "currency": "IDR",
				  "transact_method": "gopay",
				  "items": [
				    {
				      "item_id": "random",
				      "item_name": "comedic",
				      "quantity": -2,
				      "price": 20000.0
				    }
				  ],
				  "customer": {
				    "user_id": "random",
				    "first_name": "joni",
				    "last_name" : "" ,
				    "email" : "cp@example.com",
				    "phone" : "62341243212"
				  },
				  "message" : "hello"
				}""", TransactionDomain.CreateTransact.class);
			when(service.createTransaction(any(TransactionDomain.CreateTransact.class), any(ZoneId.class)))
				.thenAnswer(new TransactResponseAnswer());

			mockMvc.perform(post("/transact/create")
					.content(mapper.writeValueAsBytes(createTransact))
					.contentType(MediaType.APPLICATION_JSON)
				)
				.andExpectAll(status().is4xxClientError(),
					jsonPath("$.message[1].property_name").value("items[0].quantity"),
					jsonPath("$.message[1].message").value("must be positive and not zero"),
					jsonPath("$.message[1].provide_value").value("-2"),
					jsonPath("$.message[0].provide_value").value("-0.0"),
					jsonPath("$.message[0].property_name").value("gross_amount"),
					jsonPath("$.message[0].message").value("must be positive and not zero")
				)
				.andDo(print());

		});
	}

	static class TransactResponseAnswer implements Answer<TransactionDomain.Response> {

		@Override
		public TransactionDomain.Response answer(InvocationOnMock invocationOnMock) throws Throwable {
			TransactionDomain.CreateTransact argument1 = invocationOnMock.getArgument(0, TransactionDomain.CreateTransact.class);
			return TransactionDomain.Response
				.builder()
				.grossAmount(argument1.grossAmount())
				.items(argument1.items())
				.customer(argument1.customer())
				.currency(argument1.currency())
				.transactMethod(argument1.transactMethod())
				.transactStatus("pending")
				.orderId("order-1")
				.transactOn(fixTime)
				.build();
		}
	}

	@Test
	void testCheckTransactionAreExits() {
		when(service.findTransaction(any(), any())).thenAnswer(new CheckTransactResponseAnswer());
		assertDoesNotThrow(() -> {
			mockMvc.perform(get("/transact/order-1/find"))
				.andExpectAll(
					status().isOk(),
					content().contentType(MediaType.APPLICATION_JSON),
					jsonPath("$.data.order_id").value("order-1")
				);
		});
	}

	@Test
	void testCheckTransactionAreNotExits() {
		when(service.findTransaction(any(), any())).thenAnswer(new CheckTransactResponseAnswer());
		assertDoesNotThrow(() -> {
			mockMvc.perform(get("/transact/order-2/find"))
				.andExpectAll(
					status().isNotFound(),
					content().contentType(MediaType.APPLICATION_JSON),
					jsonPath("$.message").value("transaction with order_id order-2 not exits"),
					jsonPath("$.status_code").value(404),
					jsonPath("$.status").value("failed to found the transaction")
				);
		});
	}

	static class CheckTransactResponseAnswer implements Answer<TransactionDomain.Response> {

		@Override
		public TransactionDomain.Response answer(InvocationOnMock invocationOnMock) throws Throwable {
			String argument1 = invocationOnMock.getArgument(0);
			TransactionDomain.Response response = TransactionDomain.Response
				.builder()
				.grossAmount(40_000d)
				.items(null)
				.customer(null)
				.currency(Currency.getInstance("IDR"))
				.transactMethod("alfamart")
				.transactStatus("pending")
				.orderId("order-1")
				.transactOn(fixTime)
				.build();
			if (argument1.equalsIgnoreCase(response.getOrderId()))
				return response;
			throw new TransactionNotFoundException("transaction with order_id %s not exits".formatted(argument1));
		}
	}

	@Test
	void testCancelTheTransactionSuccessFully() {
		assertDoesNotThrow(() -> {
			mockMvc.perform(post("/transact/order-1/cancel"))
				.andDo(print())
				.andExpectAll(status().isAccepted(),
					content().contentType(MediaType.APPLICATION_JSON)
				);
		});
	}

	@Test
	void testGetAllTransaction() {
		assertDoesNotThrow(() -> {
			mockMvc.perform(get("/transact/all"))
				.andExpectAll(status().isOk())
				.andDo(print());
		});
	}
}
