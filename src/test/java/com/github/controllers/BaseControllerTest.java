package com.github.controllers;

import com.github.security.SecurityConfiguration;
import com.github.security.SecurityTestConfig;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@Tag("integration-testing")
@WebMvcTest(BaseController.class)
@ContextConfiguration(classes = {
	BaseController.class,
	SecurityConfiguration.class,
	SecurityTestConfig.class
})
class BaseControllerTest {
	@Autowired
	MockMvc mock;

	@Test
	void homeEndpointAccessed() {
		final String expectResult = "home";
		assertDoesNotThrow(() -> {
			mock.perform(
					get("/")
				)
				.andExpect(status().isOk())
				.andExpect(content().string(containsString(expectResult)));
		});
	}

	@Test
	void secretEndPointAccessedMustBeAuthenticated() {
		final String expectResult = "secret";
		assertDoesNotThrow(() -> {
			mock.perform(
					get("/secret").with(httpBasic("joko", "jas"))
				).andExpect(status().isOk())
				.andExpect(content().string(containsString(expectResult)));
		});
	}

	@Test
	void secretEndpointShouldCantAccessed() {
		assertDoesNotThrow(()->{
			mock.perform(
				get("/secret").with(httpBasic("joko","jost"))
			).andExpect(status().isUnauthorized());
		});
	}
}