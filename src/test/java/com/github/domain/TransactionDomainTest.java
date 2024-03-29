package com.github.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.app.JacksonConfigurationTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Currency;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TransactionDomainTest {
	ObjectMapper mapper;
	Clock clock;
	DateTimeFormatter dateFormat;

	@BeforeEach
	void setUp() {
		dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
		mapper = JacksonConfigurationTest.getConfig()
			.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		clock = Clock.fixed(Instant.now(), ZoneId.of("Asia/Jakarta"));
	}

	@Test
	void jsonShouldSerializeToPojo() {
		String transactID = UUID.randomUUID().toString();
		String paymentID = UUID.randomUUID().toString();
		LocalDateTime now = LocalDateTime.now(clock);

		LocalDateTime transactFinishOn = now.plusDays(2);
		String transactStatus = "done";
		Currency currency = Currency.getInstance("IDR");
		float mount = 60000;
		String method = "gopay";
		TransactionDomain.Response domain = TransactionDomain.Response.builder().transactId(transactID).paymentId(paymentID).transactOn(now).transactFinishOn(transactFinishOn).transactStatus(transactStatus).items(new TransactionDomain.ItemsDomain()).customer(new TransactionDomain.CustomerDomain(null, null)).transactMethod(method).mount(mount).currency(currency).build();
		String jsonFormat = """
			{
			    "transact_id": "%s",
			    "payment_id": "%s",
			    "transact_status": "%s",
			    "mount": %f,
			    "currency": "%s",
			    "transact_on": "%s",
			    "transact_finish_on": "%s",
			    "transact_method": "%s",
			    "items": [],
			    "customer": {}}""".formatted(transactID, paymentID, transactStatus, mount, currency, now.format(dateFormat), transactFinishOn.format(dateFormat), method);

		Assertions.assertDoesNotThrow(() -> {
			JsonNode domainNode = mapper.readTree(mapper.writeValueAsString(domain));
			JsonNode jsonFormatNode = mapper.readTree(jsonFormat);
			assertEquals(jsonFormatNode, domainNode);
		});
	}
}