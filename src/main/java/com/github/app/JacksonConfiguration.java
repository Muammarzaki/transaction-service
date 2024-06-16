package com.github.app;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JacksonConfiguration {
	@Bean
	@Primary
	public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
		return builder -> builder
			.modules(new JavaTimeModule())
			.serializationInclusion(JsonInclude.Include.NON_EMPTY)
			.propertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy())
			.build();
	}
}
