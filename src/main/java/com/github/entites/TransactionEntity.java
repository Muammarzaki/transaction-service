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
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(unique = true)
	private String transact_id;
	@Column(unique = true)
	private String orderId;
	private int mount;
	private String currency;
	private Instant transactOn;
	private Instant transactFinis_on;
	private String transactMethod;
	@OneToMany(cascade = CascadeType.ALL)
	private List<ItemEntity> items;
	@ManyToOne(cascade = CascadeType.ALL)
	private CustomerInfoEntity customerInfo;

}