package com.github.helpers;

public class UndefinedPaymentMethodException extends RuntimeException {
	public UndefinedPaymentMethodException(String format) {
		super(format);
	}
}
