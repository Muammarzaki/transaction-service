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
	@GetMapping("{order_id}/find")
	public ResponseDomain checkTransaction(@PathVariable("order_id") String id, TimeZone timeZone) {
		TransactionDomain.Response response = transactServices.findTransaction(id, timeZone.toZoneId());
		return ResponseDomain.builder()
			.data(response)
			.build();
	}

	@ResponseStatus(HttpStatus.ACCEPTED)
	@PostMapping("{order_id}/cancel")
	public Object cancelTransaction(@PathVariable("order_id") String id, TimeZone timeZone) {
		TransactionDomain.Response response = transactServices.cancelTransaction(id, timeZone.toZoneId());
		return ResponseDomain.builder()
			.data(response)
			.build();
	}

	@ResponseStatus(HttpStatus.OK)
	@GetMapping("/all")
	public Object listAllTransaction(TimeZone timeZone) {
		List<TransactionDomain.Response> allTransaction = transactServices.getAllTransaction(timeZone.toZoneId());
		return ResponseDomain.builder()
			.data(allTransaction)
			.build();
	}

	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping("/create")
	public ResponseDomain createTransaction(@Valid @RequestBody TransactionDomain.CreateTransact createTransact, TimeZone timeZone) {
		TransactionDomain.Response createTransaction = transactServices.createTransaction(createTransact, timeZone.toZoneId());
		return ResponseDomain.builder()
			.data(createTransaction)
			.build();
	}
}