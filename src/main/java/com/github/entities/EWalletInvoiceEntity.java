package com.github.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "e_wallet_invoice")
public class EWalletInvoiceEntity extends InvoiceEntity {
	@Column(name = "name")
	private String name;
	@Column(name = "method", nullable = false)
	private String method;
	@Column(name = "url", nullable = false)
	private String url;
}
