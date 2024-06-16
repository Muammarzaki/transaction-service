package com.github.entities;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class InvoiceEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	private Long id;
}
