package com.github.services;

import com.github.domain.TransactionDomain;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.List;

public interface TransactionService {
	@Transactional
	TransactionDomain.Response createTransaction(TransactionDomain.CreateTransact transact, ZoneId zone);

	TransactionDomain.Response cancelTransaction(String transactId, ZoneId zone);

	List<TransactionDomain.Response> getAllTransaction(ZoneId zone);

	TransactionDomain.Response findTransaction(String transactId, ZoneId zone);
}
