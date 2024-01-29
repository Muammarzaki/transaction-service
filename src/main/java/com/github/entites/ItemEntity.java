package com.github.entites;

import jakarta.persistence.*;

@Entity
@Table(name = "item")
public record ItemEntity(
	@Id@GeneratedValue(strategy = GenerationType.IDENTITY) Long id,
	String ItemId,
	int count,
	int price
) {
}
