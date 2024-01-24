package com.github.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;


@Tag("unit-testing")
@ActiveProfiles("test")
class UserDetailServiceImpTest {
	@TempDir
	private Path tempDir;
	private final Path path = Path.of("users");
	private UserDetailServiceImp userDetailServiceImp;
	private BCryptPasswordEncoder encoder;

	@BeforeEach
	void setUp() throws IOException {
		Files.createFile(tempDir.resolve(path));
		encoder = new BCryptPasswordEncoder();
		userDetailServiceImp = new UserDetailServiceImp(encoder, tempDir.resolve(path));
	}

	@Test
	void registerNewUserCorrectly() {
		String name = "joko", pass = "jawr";
		userDetailServiceImp.registerUser(name, pass);
		UserDetails userDetails = userDetailServiceImp.loadUserByUsername(name);
		assertThat(userDetails).extracting(UserDetails::getUsername)
			.isEqualTo(name);
		assertThat(encoder.matches(pass, userDetails.getPassword()))
			.as("Password should match with raw password")
			.isTrue();
	}
}