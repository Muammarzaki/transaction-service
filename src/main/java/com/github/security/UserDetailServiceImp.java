package com.github.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UserDetailServiceImp implements UserDetailsService {
	private final PasswordEncoder encoder;
	private final Path USER_SAVE_PATH;
	private static final Pattern PATTERN = Pattern.compile("(\\S+)\\s+(\\S+)\\s+(\\S+)");

	public UserDetailServiceImp(PasswordEncoder encoder, @Value("${user.path}") Path userSavePath) {
		this.encoder = encoder;
		USER_SAVE_PATH = userSavePath;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		String name = "", pass = "";
		try (BufferedReader stream = Files.newBufferedReader(USER_SAVE_PATH)) {
			String line;
			while ((line = stream.readLine()) != null) {
				Matcher matcher = PATTERN.matcher(line);
				if (!matcher.matches())
					continue;
				if ((name = matcher.group(2)).equals(username))
					pass = matcher.group(3);
				break;
			}
			if (!name.equals(username))
				throw new UsernameNotFoundException("User with name %s not exist".formatted(username));
			return User.builder()
				.username(name)
				.password(pass)
				.build();
		} catch (IOException e) {
			throw new UsernameNotFoundException(e.getMessage());
		}
	}

	public void registerUser(String username, String password) {
		String userData = String.format("1 %s %s", username, encoder.encode(password));
		try (BufferedWriter writer = Files.newBufferedWriter(USER_SAVE_PATH, StandardCharsets.UTF_8, StandardOpenOption.APPEND)) {
			writer.write(userData);
			writer.newLine();
			writer.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
