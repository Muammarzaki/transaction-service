package com.github.services;

import com.github.domain.TransactionDomain;

import java.util.List;

public interface TransactionService {
	public void createTransaction(TransactionDomain.CreateTransact dataCreate);

	public void cancelTransaction(String transactId);

	public List<TransactionDomain.Response> getAllTransaction();

	public TransactionDomain.Response checkTransaction(String transactId);
}
