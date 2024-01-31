package com.github.services;

import com.github.domain.RequestDomain;
import com.github.domain.TransactionDomain;
import org.springframework.stereotype.Service;

import java.util.List;

public interface TransactionService {
	public void createTransaction(RequestDomain.CreateTransact dataCreate);

	public void removeTransaction(String transactId);

	public List<TransactionDomain> getAllTransaction();

	public TransactionDomain checkTransaction(String transactId);
}
