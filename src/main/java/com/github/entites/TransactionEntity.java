package com.github.entites;

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
	@Column(nullable = false)
	private String orderId;
	@Column(nullable = false)
	private String transactId;
	@Column(nullable = false)
	private int mount;
	@Column(nullable = false)
	private String currency;
	@Column(nullable = false)
	private Instant transactOn;
	private Instant transactFinishOn;
	private String transactMethod;
	private String transactStatus;
	@OneToMany(cascade = CascadeType.ALL)
	private List<ItemEntity> items;
	@ManyToOne(cascade = CascadeType.ALL)
	private CustomerInfoEntity customerInfo;

}