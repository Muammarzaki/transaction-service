package com.github.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.http.HttpStatus;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit-testing")
class ResponseDomainTest {
	ObjectMapper mapper;

	@BeforeEach
	void setUp() {
		mapper = new ObjectMapper();
		mapper.setDefaultPrettyPrinter(new DefaultPrettyPrinter());
	}

	@Test
	void objectShouldHaveSomeAttributesWithSnakeCaseFormatWhenSerialize() {
		String jsonFormat = "{\"status\":\"OK\",\"status_code\":200,\"data\":{},\"message\":{}}";

		ResponseDomain domain = ResponseDomain.builder().status(HttpStatus.OK.name()).statusCode(200).message(new HashMap<>()).data(new HashMap<>()).build();

		assertDoesNotThrow(() -> {
			assertEquals(jsonFormat, mapper.writeValueAsString(domain));
		});

	}

	@Test
	public void JsonStringShouldCanDeserializeToPoJo() throws JsonProcessingException {
		String jsonFormat = "{\"status\":\"OK\",\"status_code\":200,\"data\":{},\"message\":{}}";

		ResponseDomain domain = ResponseDomain.builder().status(HttpStatus.OK.name()).statusCode(200).message(new HashMap<>()).data(new HashMap<>()).build();

		assertDoesNotThrow(() -> {
			ResponseDomain domainAfterDeserialize = mapper.readValue(jsonFormat, ResponseDomain.class);
			assertEquals(domain, domainAfterDeserialize);
		});
	}

}