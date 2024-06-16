package com.github.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Entity
@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
@Table(name = "transact")
public class TransactionEntity {
	@Id
	@Column(name = "order_id", nullable = false)
	private String orderId;
	@Column(name = "transact_id", nullable = false)
	private String transactId;
	@Column(name = "gross_amount", nullable = false)
	private double grossAmount;
	@Column(name = "currency", nullable = false)
	private String currency;
	@Column(name = "transact_on", nullable = false)
	private Instant transactOn;
	@Column(name = "transact_finish_on", nullable = true)
	private Instant transactFinishOn;
	@Column(name = "transact_method")
	private String transactMethod;
	@Column(name = "transact_status")
	private String transactStatus;
	@OneToMany(cascade = CascadeType.ALL)
	@JoinTable(
		name = "transact_with_item",
		joinColumns = @JoinColumn(name = "order_id"),
		inverseJoinColumns = @JoinColumn(name = "item_id")
	)
	private List<ItemEntity> items;
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "customer_id")
	private CustomerInfoEntity customerInfo;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "invoice_id")
	private InvoiceEntity invoice;

}