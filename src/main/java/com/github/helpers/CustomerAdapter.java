package com.github.helpers;

import com.github.domain.TransactionDomain;
import com.github.entities.CustomerInfoEntity;

public abstract class CustomerAdapter {
	private CustomerAdapter() {
	}

	public static CustomerInfoEntity convertToCustomerInfo(TransactionDomain.CustomerDomain domain) {
		return CustomerInfoEntity.builder()
			.userId(domain.userId())
			.username(domain.username())
			.build();
	}

	public static TransactionDomain.CustomerDomain convertFromCustomerInfo(CustomerInfoEntity entity) {
		return new TransactionDomain.CustomerDomain(entity.getUserId(), entity.getUsername());
	}
}
