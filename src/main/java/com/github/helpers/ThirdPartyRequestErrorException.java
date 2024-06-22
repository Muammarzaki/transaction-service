package com.github.helpers;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.HttpClientErrorException;

public class ThirdPartyRequestErrorException extends RuntimeException {
	private final HttpClientErrorException error;
	@Getter
	private final String vendor;

	public ThirdPartyRequestErrorException(HttpStatusCode statusCode, String statusText, String vendor) {
		this.vendor = vendor;
		error = new HttpClientErrorException(statusCode, statusText);
	}

	public HttpStatusCode getStatusCode() {
		return error.getStatusCode();
	}

	public String getStatusText() {
		return error.getStatusText();
	}
}
