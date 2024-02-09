package com.github.services;

import com.github.domain.TransactionDomain;

import java.util.List;

public interface TransactionService {
	public void createTransaction(TransactionDomain.Response dataCreate);

	public void removeTransaction(String transactId);

	public List<TransactionDomain> getAllTransaction();

	public TransactionDomain checkTransaction(String transactId);
}
