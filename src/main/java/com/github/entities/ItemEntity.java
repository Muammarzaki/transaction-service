package com.github.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Builder
@AllArgsConstructor
@Data
@NoArgsConstructor
@Table(name = "item")
public class ItemEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;
	@Column(name = "item_id", nullable = false)
	private String itemId;
	@Column(name = "item_name", nullable = false)
	private String itemName;
	@Column(name = "price", nullable = false)
	private double price;
	@Column(name = "quantity", nullable = false)
	private int quantity;
}
