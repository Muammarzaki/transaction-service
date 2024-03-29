package com.github.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfiguration {

	@Bean
	public SecurityFilterChain basicAuth(HttpSecurity http) throws Exception {
		return http
			.authorizeHttpRequests(auth ->
				auth
					.requestMatchers("/auth/check").authenticated()
					.requestMatchers("/auth/**").permitAll()
					.anyRequest().authenticated()
			)
			.csrf(AbstractHttpConfigurer::disable)
			.httpBasic(withDefaults())
			.build();
	}

	@Bean
	public AuthenticationManager authenticationManager(PasswordEncoder encoder, UserDetailsService userDetailsService) {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		provider.setPasswordEncoder(encoder);
		provider.setUserDetailsService(userDetailsService);
		return new ProviderManager(provider);
	}
}
