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
	private String order_id;
	private int mount;
	private String currency;
	private Instant transact_on;
	private Instant transact_finis_on;
	private String transact_method;
	@OneToMany(cascade = CascadeType.ALL)
	private List<ItemEntity> items;
	@ManyToOne(cascade = CascadeType.ALL)
	private CustomerInfoEntity customerInfo;

}