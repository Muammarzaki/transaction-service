package com.github.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JacksonConfigurationTest {

	public static ObjectMapper getConfig() {
		return new ObjectMapper().setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy())
			.registerModule(new JavaTimeModule());
	}

}