package com.github.app;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = JacksonConfiguration.class)
@EnableAutoConfiguration
public class JacksonConfigurationTest {
	@Autowired
	ObjectMapper mapper;

	public static ObjectMapper getConfig() {
		return new ObjectMapper()
			.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy())
			.setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
			.registerModule(new JavaTimeModule());
	}

	@Test
	void mapperConfigurationValidators() {
		PropertyNamingStrategy propertyNamingStrategy = mapper.getPropertyNamingStrategy();
		assertThat(propertyNamingStrategy).isInstanceOf(PropertyNamingStrategies.SnakeCaseStrategy.class);
		System.out.println(propertyNamingStrategy);
	}
}