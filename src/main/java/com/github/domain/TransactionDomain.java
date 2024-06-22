package com.github.domain;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Currency;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;


public abstract class TransactionDomain {
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class Response {
		private String orderId;
		private String transactStatus;
		@JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT, pattern = "0.00")
		private double grossAmount;
		private Currency currency;
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
		private LocalDateTime transactOn;
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
		private LocalDateTime transactFinishOn;
		private String transactMethod;
		private ItemsDomain items;
		private CustomerDomain customer;
		private String message = "";

		@JsonIgnore
		private Map<String, Object> anyProperty = new HashMap<>();

		@JsonAnySetter
		private void anyCreator(String key, Object value) {
			anyProperty.put(key, value);
		}

		@JsonAnyGetter
		private Map<String, Object> getAny() {
			return anyProperty;
		}
	}


	public static class ItemsDomain extends LinkedList<ItemsDomain.ItemDomain> {

		public double getTotalPrice() {
			return this.parallelStream()
				.mapToDouble(x -> x.quantity * x.price)
				.sum();
		}

		public record ItemDomain(
			@NotEmpty(message = "cannot empty or null")
			String itemId,
			@NotEmpty(message = "cannot empty or null")
			String itemName,
			@Positive(message = "must be positive and not zero")
			int quantity,
			@Positive(message = "must be positive and not zero")
			double price

		) {

		}
	}

	public record CustomerDomain(
		@NotBlank @NotNull String userId,
		@NotBlank @NotNull String firstName,
		@NotNull String lastName,
		@NotEmpty String email,
		@NotEmpty String phone

	) {
	}

	public record CreateTransact(
		@Positive(message = "must be positive and not zero")
		double grossAmount,
		@NotNull(message = "cannot empty or null")
		Currency currency,
		@NotEmpty(message = "cannot empty or null")
		String transactMethod,
		@NotEmpty(message = "cannot empty or null")
		@Valid
		ItemsDomain items,
		@NotNull(message = "cannot empty or null")
		@Valid
		CustomerDomain customer,
		@NotEmpty(message = "cannot empty or null")
		String message
	) {
	}
}
