package com.github.filters;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;

public class TokenInterceptors implements HandlerInterceptor {
	private final ApplicationContext context;

	public TokenInterceptors(ApplicationContext context) {
		this.context = context;
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
		context.getBean(MessageDigest.class).reset();
		HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		String token = request.getHeader("TOKEN");
		if (token.isEmpty()) {
			throw new IllegalAccessException("TOKEN REQUIRED");
		}

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String name = authentication.getName();
		String salt = context.getEnvironment().getProperty("${security.salt}", String.class);
		MessageDigest hashing = context.getBean(MessageDigest.class);

		byte[] digest = hashing.digest((salt + name + salt).getBytes(StandardCharsets.UTF_8));

		if (!token.contentEquals(Arrays.toString(digest))) {
			throw new IllegalAccessException("TOKEN not Valid");
		}

		return true;
	}
}
