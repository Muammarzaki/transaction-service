package com.github.domain;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.app.JacksonConfigurationTest;
import com.github.helpers.ResponseView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("unit-testing")
class ResponseViewDomainTest {
	ObjectMapper mapper;

	@BeforeEach
	void setUp() {
		mapper = JacksonConfigurationTest.getConfig();
		mapper.setDefaultPrettyPrinter(new DefaultPrettyPrinter());
	}

	@Test
	void objectShouldHaveSomeAttributesWithSnakeCaseFormatWhenSerialize() {
		String jsonFormat = "{\"status\":\"OK\",\"status_code\":200,\"data\":{},\"message\":{}}";

		ResponseDomain domain = ResponseDomain.builder().status(HttpStatus.OK.name()).statusCode(200).message(new HashMap<>()).data(new HashMap<>()).build();

		mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

		assertDoesNotThrow(() -> {
			assertEquals(jsonFormat, mapper.writeValueAsString(domain));
		});
	}

	@Test
	public void JsonStringShouldCanDeserializeToPoJo() throws JsonProcessingException {
		String jsonFormat = "{\"status\":\"OK\",\"status_code\":200,\"data\":{},\"message\":{}}";

		ResponseDomain domain = ResponseDomain.builder().status(HttpStatus.OK.name()).statusCode(200).message(new HashMap<>()).data(new HashMap<>()).build();

		mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

		assertDoesNotThrow(() -> {
			ResponseDomain domainAfterDeserialize = mapper.readValue(jsonFormat, ResponseDomain.class);
			assertEquals(domain, domainAfterDeserialize);
		});
	}

	@Test
	void JsonStringShouldCanDeserializeByGroupToPoJo() {
		String jsonFormat = "{\"status\":\"OK\",\"status_code\":200,\"data\":{}}";

		ResponseDomain domain = ResponseDomain.builder()
			.status(HttpStatus.OK.name())
			.statusCode(200)
			.data(new HashMap<>()).build();

		assertDoesNotThrow(() -> {
			ResponseDomain domainAfterDeserialize = mapper.readerWithView(ResponseView.Success.class).readValue(jsonFormat, ResponseDomain.class);
			assertEquals(domain, domainAfterDeserialize);
		});
		String jsonFailSample = "{\"status\":\"OK\",\"status_code\":200,\"message\":{}}";

		ResponseDomain domain2 = ResponseDomain.builder()
			.status(HttpStatus.OK.name())
			.statusCode(200)
			.message(new HashMap<>()).build();

		assertDoesNotThrow(() -> {
			ResponseDomain domainAfterDeserialize = mapper.readerWithView(ResponseView.Fail.class).readValue(jsonFailSample, ResponseDomain.class);
			assertEquals(domain2, domainAfterDeserialize);
		});
	}

}