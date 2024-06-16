package com.github.helpers;

import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.HttpClientErrorException;

public class ThirdPartyRequestErrorException extends HttpClientErrorException {

	public ThirdPartyRequestErrorException(HttpStatusCode statusCode, String statusText) {
		super(statusCode, statusText);
	}

}
