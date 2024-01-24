package com.github.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Configuration
public class Encoding {

	public static final int STRENGTH = 5;
	public static final String HASHING_ALGORITHM = "sha-256";

	@Bean
	@Primary
	public MessageDigest hashing() throws NoSuchAlgorithmException {
		return MessageDigest.getInstance(HASHING_ALGORITHM);
	}

	@Bean
	@Primary
	public PasswordEncoder bCrypt() {
		return new BCryptPasswordEncoder(STRENGTH);
	}


}
