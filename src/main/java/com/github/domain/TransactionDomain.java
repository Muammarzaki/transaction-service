package com.github.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.entites.CustomerInfoEntity;
import com.github.entites.ItemEntity;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Currency;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;


public abstract class TransactionDomain {
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class Response {
		@NotEmpty(message = "cannot empty or null")
		private String orderId;
		@NotEmpty(message = "cannot empty or null")
		private String transactId;
		@NotEmpty(message = "cannot empty or null")
		private String transactStatus;
		@NotEmpty(message = "cannot empty or null")
		@JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT, pattern = "0.00")
		@Positive(message = "must positive value")
		private double mount;
		@NotEmpty(message = "cannot empty or null")
		private Currency currency;
		@NotEmpty(message = "cannot empty or null")
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
		private LocalDateTime transactOn;
		@NotEmpty(message = "cannot empty or null")
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
		private LocalDateTime transactFinishOn;
		@NotEmpty(message = "cannot empty or null")
		private String transactMethod;
		@NotEmpty(message = "cannot empty or null")
		@Size(min = 1)
		private ItemsDomain items;
		@NotEmpty(message = "cannot empty or null")
		private CustomerDomain customer;
	}


	public static class ItemsDomain extends LinkedList<ItemsDomain.ItemDomain> {

		public double getTotalPrice() {
			return this.parallelStream()
				.mapToDouble(x -> x.count * x.price)
				.sum();
		}

		public static ItemsDomain convertFromListOfItemEntity(List<ItemEntity> items) {
			if (items == null) throw new IllegalArgumentException("Item Entity null");
			return items.stream().map(ItemDomain::convertFromItemEntity).collect(Collectors.toCollection(ItemsDomain::new));
		}

		public static List<ItemEntity> convertToItemEntity(ItemsDomain domain) {
			if (domain == null) throw new IllegalArgumentException("Items Domain null");
			return domain.stream().map(itm -> ItemEntity.builder()
					.ItemId(itm.itemId())
					.itemName(itm.itemName())
					.count(itm.count())
					.price(itm.price())
					.build())
				.collect(Collectors.toList());
		}

		public record ItemDomain(
			@NotEmpty(message = "cannot empty or null")
			String itemId,
			@NotEmpty(message = "cannot empty or null")
			String itemName,
			@Positive(message = "must be positive and not zero")
			int count,
			@Positive(message = "must be positive and not zero")
			double price

		) {
			public static ItemDomain convertFromItemEntity(ItemEntity entity) {
				if (entity == null) throw new IllegalArgumentException("Item entity null");
				return new ItemDomain(entity.getItemId(), entity.getItemName(), entity.getCount(), entity.getPrice());
			}

			public static ItemEntity convertToItemEntity(ItemDomain domain) {
				if (domain == null) throw new IllegalArgumentException("Item domain null");
				return ItemEntity.builder()
					.ItemId(domain.itemId)
					.itemName(domain.itemName)
					.price(domain.price)
					.count(domain.count)
					.build();
			}
		}
	}

	public record CustomerDomain(
		@NotBlank
		@NotNull String userId,
		@NotBlank
		@NotNull String username
	) {
		public static CustomerInfoEntity convertToCustomerInfo(CustomerDomain domain) {
			return CustomerInfoEntity.builder()
				.userId(domain.userId)
				.username(domain.username)
				.build();
		}

		public static CustomerDomain convertFromCustomerInfo(CustomerInfoEntity entity) {
			return new CustomerDomain(entity.getUserId(), entity.getUsername());
		}
	}

	public record CreateTransact(
		@Positive(message = "must be positive and not zero")
		double mount,
		@NotEmpty(message = "cannot empty or null")
		Currency currency,
		@NotEmpty(message = "cannot empty or null")
		String transactMethod,
		@NotEmpty(message = "cannot empty or null")
		ItemsDomain items,
		@NotEmpty(message = "cannot empty or null")
		CustomerDomain customer
	) {
	}
}
