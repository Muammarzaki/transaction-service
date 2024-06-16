package com.github.helpers;

import java.util.NoSuchElementException;

public class TransactionNotFoundException extends NoSuchElementException {
	public TransactionNotFoundException(String message) {
		super(message);
	}
}
