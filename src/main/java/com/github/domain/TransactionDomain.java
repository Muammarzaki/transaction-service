package com.github.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Currency;
import java.util.LinkedList;


public abstract class TransactionDomain {
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class Response {
		private String transactId;
		private String paymentId;
		private String transactStatus;
		@JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT, pattern = "0.00")
		private float mount;
		private Currency currency;
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
		private LocalDateTime transactOn;
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
		private LocalDateTime transactFinishOn;
		private String transactMethod;
		private ItemsDomain items;
		private CustomerDomain customer;

	}


	public static class ItemsDomain extends LinkedList<ItemsDomain.ItemDomain> {

		public int getTotalPrice() {
			return this.parallelStream()
				.mapToInt(x -> x.count * x.price)
				.sum();
		}

		public record ItemDomain(
			String itemId,
			String itemName,
			int count,
			int price

		) {
		}
	}

	public record CustomerDomain(
		@NotBlank
		@NotNull String userId,
		@NotBlank
		@NotNull String username
	) {
	}

	public record CreateTransact(
		int mount,
		Currency currency,
		String transactMethod,
		ItemsDomain items,
		CustomerDomain customer
	) {
	}
}
