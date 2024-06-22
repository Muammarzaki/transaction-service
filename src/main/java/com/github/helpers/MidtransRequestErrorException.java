package com.github.helpers;

import org.springframework.http.HttpStatusCode;

public class MidtransRequestErrorException extends ThirdPartyRequestErrorException {
	public MidtransRequestErrorException(HttpStatusCode statusCode, String statusText) {
		super(statusCode, statusText, "midtrans");
	}
}
