package com.github.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.app.JacksonConfigurationTest;
import com.github.helpers.UndefinedPaymentMethodException;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("unit-testing")
class MidtransDomainTest {
	MidtransDomain.TransactionRequest cStoreRequest;
	MidtransDomain.TransactionRequest bankRequest;
	MidtransDomain.TransactionRequest eWalletRequest;

	ObjectMapper mapper;

	@BeforeEach
	void setUp() {
		mapper = JacksonConfigurationTest.getConfig();

		MidtransDomain.TransactionDetails transactionDetails = new MidtransDomain.TransactionDetails("order-101", 44000);

		MidtransDomain.TransactionRequest.TransactionRequestBuilder transactionRequestBuilder = MidtransDomain.TransactionRequest.builder()
			.paymentType(MidtransDomain.PaymentMethod.QRIS)
			.transactionDetails(transactionDetails);

		eWalletRequest = transactionRequestBuilder.build();
		bankRequest = transactionRequestBuilder.build();
		cStoreRequest = transactionRequestBuilder.build();
	}

	@Test
	void shouldMapperToCStoreDomain() {
		String cstoreResponse = """
			{
			  "status_code": "201",
			  "status_message": "Success, cstore transaction is successful",
			  "transaction_id": "d615df87-c96f-4f5c-9d35-2d740d54c1a9",
			  "order_id": "order-101o-1578557780",
			  "merchant_id": "G812785002",
			  "gross_amount": "162500.00",
			  "currency": "IDR",
			  "payment_type": "cstore",
			  "transaction_time": "2020-01-09 15:16:19",
			  "transaction_status": "pending",
			  "fraud_status": "accept",
			  "payment_code": "8127740588870520",
			  "store": "alfamart",
			  "expiry_time": "2024-06-21 12:21:59"
			}""";
		assertDoesNotThrow(() -> {
			MidtransDomain.TransactionResponse transactionResponse = mapper.readValue(cstoreResponse, MidtransDomain.TransactionResponse.class);
			assertThat(transactionResponse).isInstanceOf(MidtransDomain.CStoreResponse.class)
				.extracting("paymentCode", "statusCode").contains("8127740588870520", 201);
		});
	}


	@Test
	void shouldMapperToBankTransferDomain() {
		String bankResponse = """
			{
			  "status_code": "201",
			  "status_message": "Success, Bank Transfer transaction is created",
			  "transaction_id": "be03df7d-2f97-4c8c-a53c-8959f1b67295",
			  "order_id": "1571823229",
			  "merchant_id": "G812785002",
			  "gross_amount": "44000.00",
			  "currency": "IDR",
			  "payment_type": "bank_transfer",
			  "transaction_time": "2019-10-23 16:33:49",
			  "transaction_status": "pending",
			  "va_numbers": [
			    {
			      "bank": "bca",
			      "va_number": "812785002530231"
			    }
			  ],
			  "fraud_status": "accept",
			   "expiry_time": "2024-06-21 12:21:59"
			}""";
		assertDoesNotThrow(() -> {
			MidtransDomain.TransactionResponse transactionResponse = mapper.readValue(bankResponse, MidtransDomain.TransactionResponse.class);
			assertThat(transactionResponse).isInstanceOf(MidtransDomain.BankTransferResponse.class)
				.asInstanceOf(InstanceOfAssertFactories.type(MidtransDomain.BankTransferResponse.class))
				.extracting(MidtransDomain.BankTransferResponse::getVaNumbers).isNotNull()
				.asInstanceOf(InstanceOfAssertFactories.list(MidtransDomain.EWalletResponse.Action.class))
				.hasSize(1);
		});
	}

	@Test
	void shouldMapperToEWalletDomain() {
		String eWalletResponse = """
			{
			  "status_code": "201",
			  "status_message": "GO-PAY transaction is created",
			  "transaction_id": "231c79c5-e39e-4993-86da-cascade56c1d",
			  "order_id": "order-101h-1570513296",
			  "gross_amount": "44000.00",
			  "currency": "IDR",
			  "payment_type": "qris",
			  "transaction_time": "2019-10-08 12:41:36",
			  "transaction_status": "pending",
			  "fraud_status": "accept",
			  "actions": [
			    {
			      "name": "generate-qr-code",
			      "method": "GET",
			      "url": "https://api.sandbox.veritrans.co.id/v2/gopay/231c79c5-e39e-4993-86da-cadcaee56c1d/qr-code"
			    },
			    {
			      "name": "deeplink-redirect",
			      "method": "GET",
			      "url": "https://simulator.sandbox.midtrans.com/gopay/ui/checkout?referenceid=Y0xwjoQ9uy&callback_url=someapps%3A%2F%2Fcallback%3Forder_id%3Dorder-101h-1570513296"
			    },
			    {
			      "name": "get-status",
			      "method": "GET",
			      "url": "https://api.sandbox.veritrans.co.id/v2/231c79c5-e39e-4993-86da-cadcaee56c1d/status"
			    },
			    {
			      "name": "cancel",
			      "method": "POST",
			      "url": "https://api.sandbox.veritrans.co.id/v2/231c79c5-e39e-4993-86da-cadcaee56c1d/cancel"
			    }
			  ],
			   "expiry_time": "2024-06-21 12:21:59"
			}""";

		assertDoesNotThrow(() -> {
			MidtransDomain.TransactionResponse transactionResponse = mapper.readValue(eWalletResponse, MidtransDomain.TransactionResponse.class);
			assertThat(transactionResponse).isInstanceOf(MidtransDomain.EWalletResponse.class)
				.asInstanceOf(InstanceOfAssertFactories.type(MidtransDomain.EWalletResponse.class))
				.extracting(MidtransDomain.EWalletResponse::getActions)
				.asInstanceOf(InstanceOfAssertFactories.list(MidtransDomain.EWalletResponse.Action.class))
				.hasSize(4);

			assertThat(transactionResponse.getStatusCode()).isEqualTo(201);
		});
	}

	@Test
	void shouldMapperToCStoreTransactionRequestDomain() {
		cStoreRequest.setAnyProperties(Map.of("cstore", new MidtransDomain.Cstore("alfamart", "test")));
		assertDoesNotThrow(() -> {
			String requestBody = mapper.writeValueAsString(cStoreRequest);
			assertThat(requestBody)
				.contains("cstore", "alfamart", "message");
			System.out.print(requestBody);
		});
	}

	@Test
	void shouldMapperToBankTransferTransactionRequestDomain() {
		bankRequest.setAnyProperties(Map.of("bank_transfer", new MidtransDomain.BankTransfer("bca")));
		assertDoesNotThrow(() -> {
			String requestBody = mapper.writeValueAsString(bankRequest);
			assertThat(requestBody)
				.contains("bank_transfer", "bank", "bca");
			System.out.print(requestBody);
		});
	}

	@Test
	void shouldMapperToEWalletTransactionRequestDomain() {
		assertDoesNotThrow(() -> {
			String requestBody = mapper.writeValueAsString(eWalletRequest);
			assertThat(requestBody)
				.contains("qris", "transaction_details", "order-101");
			System.out.print(requestBody);

			MidtransDomain.TransactionRequest transactionRequest = mapper.readValue(requestBody, MidtransDomain.TransactionRequest.class);
			assertThat(transactionRequest).isEqualTo(eWalletRequest);
		});
	}

	@Test
	void paymentMethodShouldGeneratedByType() {
		assertDoesNotThrow(() -> {
			MidtransDomain.PaymentMethod method = MidtransDomain.PaymentMethod.of("cstore");
			assertThat(method).isEqualTo(MidtransDomain.PaymentMethod.CSTORE);
		});
	}

	@Test
	void paymentMethodCannotCreateUnsupportedPaymentType() {
		UndefinedPaymentMethodException thrown = assertThrows(UndefinedPaymentMethodException.class, () -> {
			MidtransDomain.PaymentMethod.of("cs");
		});
		assertThat(thrown.getMessage())
			.isEqualToIgnoringCase("Unknown payment method type: cs");
	}

	@Test
	void paymentMethodShouldGeneratedBySubType() {
		MidtransDomain.PaymentMethod method = MidtransDomain.PaymentMethod.fromSubType("alfamart");
		assertThat(method)
			.isEqualTo(MidtransDomain.PaymentMethod.CSTORE);
	}

	@Test
	void paymentMethodShouldNotGeneratedBySubType() {
		UndefinedPaymentMethodException thrown = assertThrows(UndefinedPaymentMethodException.class, () -> {
			MidtransDomain.PaymentMethod.fromSubType("toko cina");
		});
		assertThat(thrown.getMessage())
			.isEqualToIgnoringCase("Unknown payment method from sub-type: toko cina");
	}

	@Test
	void creditCardShouldMappedToJson() {
		MidtransDomain.CreditCard creditCard = new MidtransDomain.CreditCard("ichikiwir", true);
		assertDoesNotThrow(() -> {
			String creditCardJson = mapper.writeValueAsString(creditCard);
			assertThat(creditCardJson)
				.contains("token_id", "authentication");
		});
	}

	@Test
	void shouldSerializeCustomerDetailsCorrectly() {
		MidtransDomain.CustomerDetails customerDetails = new MidtransDomain.CustomerDetails("joko", null, "example@gmail.com", "628124423242");
		assertDoesNotThrow(() -> {
			String customerDetailsJson = mapper.writeValueAsString(customerDetails);
			assertThat(customerDetailsJson)
				.contains("name", "email");
		});
	}

}