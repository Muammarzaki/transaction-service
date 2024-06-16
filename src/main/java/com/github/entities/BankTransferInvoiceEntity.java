package com.github.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "bank_transfer_invoice")
public class BankTransferInvoiceEntity extends InvoiceEntity {
	@Column(name = "va_number", nullable = false)
	private String vaNumber;
	@Column(name = "bank", nullable = false)
	private String bank;
}
