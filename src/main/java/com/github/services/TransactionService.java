package com.github.services;

import com.github.domain.TransactionDomain;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.List;

public interface TransactionService {
	@Transactional
	TransactionDomain.Response createTransaction(TransactionDomain.CreateTransact transact, ZoneId zoneId);

	public void cancelTransaction(String transactId);

	public List<TransactionDomain.Response> getAllTransaction(ZoneId zoneId);

	public TransactionDomain.Response checkTransaction(String transactId, ZoneId zoneId);
}
