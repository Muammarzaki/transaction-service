package com.github.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "cstore_invoice")
public class CStoreInvoiceEntity extends InvoiceEntity {
	@Column(name = "payment_code", nullable = false)
	private String paymentCode;
	@Column(name = "store", nullable = false)
	private String store;
}
