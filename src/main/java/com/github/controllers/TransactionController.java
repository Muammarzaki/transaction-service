package com.github.controllers;

import com.github.domain.ResponseDomain;
import com.github.domain.TransactionDomain;
import com.github.services.TransactionService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.TimeZone;

@RestController
@RequestMapping("transact")
@Slf4j
public class TransactionController {

	private final TransactionService transactServices;

	public TransactionController(TransactionService transactServices) {
		this.transactServices = transactServices;
	}

	@ResponseStatus(HttpStatus.OK)
	@GetMapping("{order_id}/check")
	public ResponseDomain checkTransaction(@PathVariable("order_id") String id, TimeZone timeZone) {
		TransactionDomain.Response response = transactServices.checkTransaction(id, timeZone.toZoneId());
		return ResponseDomain.builder()
			.statusCode(HttpStatus.OK.value())
			.status("order_id exits")
			.data(response)
			.build();
	}

	@ResponseStatus(HttpStatus.ACCEPTED)
	@PostMapping("{order_id}/cancel")
	public Object cancelTransaction(@PathVariable("order_id") String id) {
		transactServices.cancelTransaction(id);
		return ResponseDomain.builder()
			.statusCode(HttpStatus.ACCEPTED.value())
			.status("the transaction with order id %s has ben deleted".formatted(id))
			.build();
	}

	@ResponseStatus(HttpStatus.OK)
	@GetMapping("/all")
	public Object listAllTransaction(TimeZone timeZone) {
		List<TransactionDomain.Response> allTransaction = transactServices.getAllTransaction(timeZone.toZoneId());
		return ResponseDomain.builder()
			.statusCode(HttpStatus.OK.value())
			.status(String.format("all of transaction from database with size %d", allTransaction.size()))
			.data(allTransaction)
			.build();
	}

	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping("/create")
	public ResponseDomain createTransaction(@Valid @RequestBody TransactionDomain.CreateTransact createTransact, TimeZone timeZone) {
		TransactionDomain.Response createTransaction = transactServices.createTransaction(createTransact, timeZone.toZoneId());
		return ResponseDomain.builder()
			.statusCode(201)
			.data(createTransaction)
			.status("transaction successfully created")
			.build();
	}
}