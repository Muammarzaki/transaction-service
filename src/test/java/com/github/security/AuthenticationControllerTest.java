package com.github.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.TransactionServiceApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class AuthenticationControllerTest {
	@Nested
	class RegisterDomainTest {
		@Test
		void itsPasswordCanDecodeByBase64() {
			String passExpect = "a secret word";
			String username = "joko";
			AuthenticationController.RegisterDomain domain = new AuthenticationController.RegisterDomain(username, Base64.getEncoder().encodeToString(passExpect.getBytes()));
			assertThat(domain).extracting(AuthenticationController.RegisterDomain::password).isEqualTo(passExpect);
		}
	}

	@Nested
	@Tag("integration-testing")
	@ActiveProfiles("test")
	@WebMvcTest(AuthenticationController.class)
	@ContextConfiguration(classes = {
		TransactionServiceApplication.class,
		SecurityConfiguration.class,
		Encoding.class,
		UserDetailServiceImp.class
	})
	class RegisterServiceTest {
		@Autowired
		MockMvc mock;

		@MockBean
		UserDetailServiceImp userDetailServiceImp;

		ObjectMapper mapper;

		@BeforeEach
		void setUp() {
			mapper = new ObjectMapper();
		}

		@Test
		void saveNewUserCorrectly() {
			String passExpect = "foo";
			String username = "foo";
			Map<String, String> domain = new HashMap<>();
			domain.put("username", username);
			domain.put("password", Base64.getEncoder().encodeToString(passExpect.getBytes()));


			assertDoesNotThrow(() -> {
				String dataRequest = mapper.writeValueAsString(domain);
				mock.perform(
					post("/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(dataRequest)
				).andExpect(status().isAccepted());
			});
			verify(userDetailServiceImp,times(1)).registerUser(anyString(),anyString());
		}

	}
}