package com.github.services;

import com.github.domain.TransactionDomain;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class MidtransTransactionImpl implements TransactionService{

	@Override
	public void createTransaction(TransactionDomain.Response dataCreate) {

	}

	@Override
	public void removeTransaction(String transactId) {

	}

	@Override
	public List<TransactionDomain> getAllTransaction() {
		return null;
	}

	@Override
	public TransactionDomain checkTransaction(String transactId) {
		return null;
	}
}
