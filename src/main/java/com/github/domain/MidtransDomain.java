package com.github.domain;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Map;

public abstract class MidtransDomain {
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class TransactionRequest {
		private String paymentType;
		private TransactionDetails transactionDetails;
		private CustomerDetails customerDetails;
		private TransactionDomain.ItemsDomain items;
		private Map<String, Object> anyProperties;

		@JsonAnySetter
		public void addAny(String key, Object value) {
			anyProperties.put(key, value);
		}

		@JsonAnyGetter
		public Map<String, Object> getAny() {
			return anyProperties;
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "payment_type")
	@JsonSubTypes({
		@JsonSubTypes.Type(value = CStoreResponse.class, name = "cstore"),
		@JsonSubTypes.Type(value = EWalletResponse.class, name = "qris"),
		@JsonSubTypes.Type(value = EWalletResponse.class, name = "gopay"),
		@JsonSubTypes.Type(value = BankTransferResponse.class, name = "bank_transfer")
	})
	public static class TransactionResponse {
		@NotNull(message = "must provide the value")
		@PositiveOrZero(message = "gross amount should have 0 or greater")
		int grossAmount;
		@NotBlank(message = "cannot blank or null")
		String orderId;
		@NotBlank(message = "cannot blank or null")
		Currency Currency;
		@NotBlank(message = "cannot blank or null")
		@JsonProperty("payment_type")
		String paymentType;
		@NotBlank(message = "cannot blank or null")
		String transactionId;
		@NotBlank(message = "cannot blank or null")
		String transactionStatus;
		@NotBlank(message = "cannot blank or null")
		String transactionTime;
		@NotBlank(message = "cannot blank or null")
		String fraudStatus;

	}

	@JsonTypeName("cstore")
	@EqualsAndHashCode(callSuper = true)
	@Data
	@NoArgsConstructor
	@Builder
	@AllArgsConstructor
	public static class CStoreResponse extends TransactionResponse {
		private String paymentCode;
		private String store;
	}

	@JsonTypeName("qris")
	@EqualsAndHashCode(callSuper = true)
	@Data
	@NoArgsConstructor
	@Builder
	@AllArgsConstructor
	public static class EWalletResponse extends TransactionResponse {
		private List<Action> actions;

		public record Action(
			String name,
			String method,
			String url
		) {
		}
	}

	@JsonTypeName("bank_transfer")
	@EqualsAndHashCode(callSuper = true)
	@Data
	@NoArgsConstructor
	@Builder
	@AllArgsConstructor
	public static class BankTransferResponse extends TransactionResponse {
		private List<VaNumber> vaNumbers;

		@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
		public record VaNumber(
			String bank,
			String vaNumber
		) {
		}
	}


	@JsonIgnoreProperties(ignoreUnknown = true)
	public record TransactionDetails(
		@NotNull(message = "must provide the value")
		@PositiveOrZero(message = "gross amount should have 0 or greater")
		int grossAmount,
		@NotBlank(message = "cannot blank or null")
		String orderId
	) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record CustomerDetails(
		@NotBlank(message = "cannot blank or null")
		String name,
		@NotBlank(message = "cannot blank or null")
		@Email(message = "email not valid")
		String email
	) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Items(
		@NotBlank(message = "cannot blank or null")
		String itemId,
		@NotBlank(message = "cannot blank or null")
		String itemName,
		@Min(value = 1, message = "Minimum values is one")
		@Positive(message = "must positive value")
		int count,
		@Min(value = 1, message = "Minimum values is one")
		@Positive(message = "must positive value")
		int price) {
	}

	@Getter
	public enum PaymentMethod {
		CREDIT_CARD("credit_card", Collections.emptyList()),
		CSTORE("cstore", List.of("alfamart", "indomaret")),
		QRIS("qris", List.of("gopay", "qris")),
		BANK_TRANSFER("bank_transfer", List.of("bni"));

		private final String type;
		private final List<String> subType;

		PaymentMethod(String creditCard, List<String> subType) {
			type = creditCard;
			this.subType = subType;
		}

		@JsonValue
		private String type() {
			return type;
		}

		@JsonCreator
		public static PaymentMethod of(String type) {
			for (PaymentMethod method : PaymentMethod.values()) {
				if (method.type.equalsIgnoreCase(type)) {
					return method;
				}
			}
			throw new IllegalArgumentException(String.format("Unknown PaymentMethod type: %s", type));
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Cstore(
		@NotBlank(message = "cannot blank or null")
		@Pattern(regexp = "indomaret|alfamart")
		String store,
		String message
	) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record BankTransfer(String bank) {
	}
}
