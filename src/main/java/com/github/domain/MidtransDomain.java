package com.github.domain;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.github.helpers.UndefinedPaymentMethodException;
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
		private double grossAmount;
		@JsonProperty(required = true)
		private String orderId;
		private Currency currency;
		@JsonProperty(required = true)
		private PaymentMethod paymentType;
		@JsonProperty(required = true)
		private String transactionId;
		private String transactionStatus;
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
		private LocalDateTime transactionTime;
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
		String orderId,
		@JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT, pattern = "0.00")
		double grossAmount
	) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record CustomerDetails(
		String firsName,
		String lastName,
		String email,
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
		String store,
		String message
	) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record BankTransfer(String bank) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record CreditCard(String tokenId, boolean authentication) {
	}

}
