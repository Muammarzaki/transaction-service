package com.github.entites;

import jakarta.persistence.*;

@Entity
@Table(name = "customer_info")
public record CustomerInfoEntity(
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id,
	String userId,
	String username
) {
}
