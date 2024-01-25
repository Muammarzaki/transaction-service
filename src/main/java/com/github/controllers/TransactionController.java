package com.github.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("transact")
public class TransactionController {
	@ResponseStatus(HttpStatus.OK)
	public Object checkTransaction(int id) {

        return null;
    }

	@ResponseStatus(HttpStatus.NO_CONTENT)
	public Object cancelTransaction(String id) {

        return null;
    }

	@ResponseStatus(HttpStatus.OK)
	public Object listAllTransaction() {

        return null;
    }

	@ResponseStatus(HttpStatus.CREATED)
	public Object createTransaction() {

        return null;
    }
}
