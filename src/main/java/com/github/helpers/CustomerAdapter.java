package com.github.helpers;

import com.github.domain.TransactionDomain;
import com.github.entities.CustomerInfoEntity;

public abstract class CustomerAdapter {
	private CustomerAdapter() {
	}

	public static CustomerInfoEntity convertToCustomerInfo(TransactionDomain.CustomerDomain domain) {
		return CustomerInfoEntity.builder()
			.userId(domain.userId())
			.firstName(domain.firstName())
			.lastName(domain.lastName())
			.email(domain.email())
			.phone(domain.phone())
			.build();
	}

	public static TransactionDomain.CustomerDomain convertFromCustomerInfo(CustomerInfoEntity entity) {
		return new TransactionDomain.CustomerDomain(entity.getUserId(), entity.getFirstName(), entity.getLastName(), entity.getEmail(), entity.getPhone());
	}
}
