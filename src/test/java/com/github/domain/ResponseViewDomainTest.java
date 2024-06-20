package com.github.domain;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.app.JacksonConfigurationTest;
import com.github.helpers.ResponseView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("unit-testing")
class ResponseViewDomainTest {
	ObjectMapper mapper;

	@BeforeEach
	void setUp() {
		mapper = JacksonConfigurationTest.getConfig().setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
		mapper.setDefaultPrettyPrinter(new DefaultPrettyPrinter());
	}

	@Test
	void objectShouldHaveSomeAttributesWithSnakeCaseFormatWhenSerialize() {
		String jsonFormat = "{\"data\":{},\"errors\":{}}";

		ResponseDomain domain = ResponseDomain.builder().errors(new HashMap<>()).data(new HashMap<>()).build();

		mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

		assertDoesNotThrow(() -> {
			assertEquals(jsonFormat, mapper.writeValueAsString(domain));
		});
	}

	@Test
	void JsonStringShouldCanDeserializeToPoJo() {
		String jsonFormat = "{\"data\":{},\"errors\":{}}";

		ResponseDomain domain = ResponseDomain.builder().errors(new HashMap<>()).data(new HashMap<>()).build();

		mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

		assertDoesNotThrow(() -> {
			ResponseDomain domainAfterDeserialize = mapper.readValue(jsonFormat, ResponseDomain.class);
			assertEquals(domain, domainAfterDeserialize);
		});
	}

	@Test
	void JsonStringShouldCanDeserializeByGroupToPoJo() {
		String jsonFormat = "{\"data\":{}}";

		ResponseDomain domain = ResponseDomain.builder()
			.data(new HashMap<>()).build();

		assertDoesNotThrow(() -> {
			ResponseDomain domainAfterDeserialize = mapper.readerWithView(ResponseView.Success.class).readValue(jsonFormat, ResponseDomain.class);
			assertEquals(domain, domainAfterDeserialize);
		});
		String jsonFailSample = "{\"errors\":{}}";

		ResponseDomain domain2 = ResponseDomain.builder()
			.errors(new HashMap<>()).build();

		assertDoesNotThrow(() -> {
			ResponseDomain domainAfterDeserialize = mapper.readerWithView(ResponseView.Fail.class).readValue(jsonFailSample, ResponseDomain.class);
			assertEquals(domain2, domainAfterDeserialize);
		});
	}

}