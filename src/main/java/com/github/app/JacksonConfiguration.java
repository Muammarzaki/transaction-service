package com.github.app;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfiguration {
	@Bean
	public Jackson2ObjectMapperBuilderCustomizer JacksonCustomizer() {
		return builder -> builder
			.modules(new JavaTimeModule())
			.propertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy())
			.build();
	}
}
