package com.github.security;

import jakarta.servlet.FilterChain;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class Config {
	private static final int PASS_STRENGTH = 25;

	@Bean
	SecurityFilterChain basicAuth(HttpSecurity http) throws Exception {
		return http
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/actuator/**")
				.permitAll()
				.anyRequest()
				.fullyAuthenticated()
			)
			.httpBasic(withDefaults())
			.build();

	}

	AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}
@Bean
	UserDetailsService userService(){
		InMemoryUserDetailsManager userDetail = new InMemoryUserDetailsManager();
		UserDetails user = User.builder()
			.username("joko")
			.password("#esad")
			.build();
		userDetail.createUser(user);
		return  userDetail;
	}
@Bean
	PasswordEncoder passEncoder(){
		return new BCryptPasswordEncoder(PASS_STRENGTH);
	}
}
