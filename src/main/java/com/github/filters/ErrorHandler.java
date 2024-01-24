package com.github.filters;

import org.springframework.web.bind.annotation.ExceptionHandler;

public class ErrorHandler {
	@ExceptionHandler(IllegalAccessException.class)
	public String illegalAccessError(IllegalAccessException e) {
		return e.getMessage();
	}
}
