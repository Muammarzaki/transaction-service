package com.github.entites;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.List;

@Entity(name = "transact")
public record TransactionEntity(
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id,
	String transact_id,
	String order_id,
	int mount,
	String currency,
	Instant transact_on,
	Instant transact_finis_on,
	String transact_method,
	@OneToMany List<ItemEntity> items,
	@ManyToOne CustomerInfoEntity customerInfo
) {
}