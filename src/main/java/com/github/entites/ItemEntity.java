package com.github.entites;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
	@Column(nullable = false)
	private String ItemId;
	@Column(nullable = false)
	private String itemName;
	@Column(nullable = false)
	private double price;
	@Column(nullable = false)
	private int count;
}
