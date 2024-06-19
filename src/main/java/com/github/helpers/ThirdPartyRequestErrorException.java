package com.github.helpers;

import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.HttpClientErrorException;

public class ThirdPartyRequestErrorException extends RuntimeException {
	private final HttpClientErrorException error;

	public ThirdPartyRequestErrorException(HttpStatusCode statusCode, String statusText) {
		error = new HttpClientErrorException(statusCode, statusText);
	}

	public HttpStatusCode getStatusCode() {
		return error.getStatusCode();
	}

	public String getStatusText() {
		return error.getStatusText();
	}
}
