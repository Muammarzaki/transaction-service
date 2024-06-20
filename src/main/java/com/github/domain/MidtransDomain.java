package com.github.domain;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.github.helpers.UndefinedPaymentMethodException;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;

public abstract class MidtransDomain {
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class TransactionRequest {
		private PaymentMethod paymentType;
		private TransactionDetails transactionDetails;
		private CustomerDetails customerDetails;
		private TransactionDomain.ItemsDomain items;

		@JsonIgnore
		private Map<String, Object> anyProperties;

		@JsonAnySetter
		public void addAny(String key, Object value) {
			if (anyProperties == null)
				anyProperties = new HashMap<>();
			anyProperties.put(key, value);
		}

		@JsonAnyGetter
		public Map<String, Object> getAny() {
			return anyProperties;
		}
	}

	@Data
	public static class StatusResponse {
		@NotNull(message = "cannot null")
		private int statusCode;
		private String statusMessage;
	}

	@EqualsAndHashCode(callSuper = true)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "payment_type", visible = true)
	@JsonSubTypes({
		@JsonSubTypes.Type(value = CStoreResponse.class, name = "cstore"),
		@JsonSubTypes.Type(value = EWalletResponse.class, name = "qris"),
		@JsonSubTypes.Type(value = EWalletResponse.class, name = "gopay"),
		@JsonSubTypes.Type(value = BankTransferResponse.class, name = "bank_transfer")
	})
	@Data
	public abstract static class TransactionResponse extends StatusResponse {
		@NotNull(message = "must provide the value")
		@PositiveOrZero(message = "gross amount should have 0 or greater")
		private double grossAmount;
		@NotBlank(message = "cannot blank or null")
		@JsonProperty(required = true)
		private String orderId;
		@NotNull(message = "cannot blank or null")
		private Currency currency;
		@NotNull(message = "cannot blank or null")
		@JsonProperty(required = true)
		private PaymentMethod paymentType;
		@NotBlank(message = "cannot blank or null")
		@NotEmpty(message = "cannot empty")
		@JsonProperty(required = true)
		private String transactionId;
		@NotBlank(message = "cannot blank or null")
		private String transactionStatus;
		@NotNull(message = "cannot blank or null")
		@PastOrPresent(message = "must past or present value")
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
		private LocalDateTime transactionTime;
		@NotBlank(message = "cannot blank or null")
		private String fraudStatus;
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
		private LocalDateTime expiryTime;
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
			@NotEmpty(message = "cannot empty") String name,
			@NotEmpty(message = "cannot empty") String method,
			@NotEmpty(message = "cannot empty") String url
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
			@NotEmpty(message = "cannot empty") String bank,
			@NotEmpty(message = "cannot empty") String vaNumber
		) {
		}
	}


	@JsonIgnoreProperties(ignoreUnknown = true)
	public record TransactionDetails(
		@NotBlank(message = "cannot blank or null")
		String orderId,
		@NotNull(message = "must provide the value")
		@PositiveOrZero(message = "gross amount should have 0 or greater")
		@JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT, pattern = "0.00")
		double grossAmount
	) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record CustomerDetails(
		@NotBlank(message = "cannot blank or null")
		String firsName,
		String lastName,
		@Email(message = "email not valid")
		String email,
		@Pattern(regexp = "^\\+?\\d{10,15}$")
		String phone
	) {
	}

	public enum PaymentMethod {
		CREDIT_CARD("credit_card", Collections.emptyList()),
		CSTORE("cstore", List.of("alfamart", "indomaret")),
		QRIS("qris", Collections.emptyList()),
		BANK_TRANSFER("bank_transfer", List.of("bca", "bni", "bni", "bri", "cimb"));

		private final String type;
		private final List<String> subType;

		PaymentMethod(String type, List<String> subType) {
			this.type = type;
			if (subType.isEmpty()) {
				this.subType = List.of(type);
				return;
			}
			this.subType = subType;
		}

		@JsonValue
		public String getType() {
			return type;
		}

		@JsonCreator
		public static PaymentMethod of(String type) {
			for (PaymentMethod method : PaymentMethod.values()) {
				if (method.type.equalsIgnoreCase(type)) {
					return method;
				}
			}
			throw new UndefinedPaymentMethodException(String.format("Unknown payment method type: %s", type));
		}

		public static PaymentMethod fromSubType(String subType) {
			for (PaymentMethod method : PaymentMethod.values()) {
				if (method.subType.contains(subType)) {
					return method;
				}
			}
			throw new UndefinedPaymentMethodException(String.format("Unknown payment method from sub-type: %s", subType));
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
	public record BankTransfer(@NotEmpty(message = "cannot empty") String bank) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record CreditCard(@NotEmpty(message = "cannot empty") String tokenId, boolean authentication) {
	}

}
