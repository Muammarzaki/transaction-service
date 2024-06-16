package com.github.entities;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@DataJpaTest
@ActiveProfiles("test")
@Tag("integration-testing")
class TransactionEntityTest {
	@Autowired
	TestEntityManager entityManager;

	@Test
	@Order(2)
	void shouldSaveNewTransactionWithExitsCustomerAndItem() {
		CustomerInfoEntity customerInfo = CustomerInfoEntity.builder()
			.userId("2434")
			.username("joko")
			.build();
		ItemEntity item1 = ItemEntity.builder().itemId("3242")
			.itemName("foobar")
			.price(10000)
			.quantity(2)
			.build();
		TransactionEntity transactionData = TransactionEntity.builder()
			.transactId("2323")
			.grossAmount(20000)
			.orderId("order-1")
			.currency("IDR")
			.transactOn(Instant.ofEpochMilli(1706617931617L))
			.transactMethod("cash")
			.customerInfo(customerInfo)
			.items(List.of(
				item1
			))
			.build();


		assertDoesNotThrow(() -> {
			entityManager.persistAndFlush(customerInfo);
			entityManager.persistAndFlush(item1);

			assertThat(entityManager.getEntityManager().contains(customerInfo)).isTrue();
			assertThat(entityManager.getEntityManager().contains(item1)).isTrue();

			TransactionEntity savedTransaction = entityManager.persistAndFlush(transactionData);
			assertThat(savedTransaction).extracting(TransactionEntity::getCustomerInfo).extracting(CustomerInfoEntity::getUserId)
				.isNotNull().isEqualTo(transactionData.getCustomerInfo().getUserId());

			assertThat(savedTransaction).extracting(TransactionEntity::getItems).asList().hasSize(1).containsOnly(item1);

		});

	}

	@Test
	@Order(2)
	void shouldSaveNewTransactionWithExitsCustomerAndItemAndInvoice() {
		CustomerInfoEntity customerInfo = CustomerInfoEntity.builder()
			.userId("2434")
			.username("joko")
			.build();
		ItemEntity item1 = ItemEntity.builder().itemId("3242")
			.itemName("foobar")
			.price(10000)
			.quantity(2)
			.build();
		InvoiceEntity invoice = CStoreInvoiceEntity.builder()
			.store("alfamart")
			.paymentCode("123456789")
			.build();
		TransactionEntity transactionData = TransactionEntity.builder()
			.transactId("2323")
			.grossAmount(20000)
			.orderId("order-1")
			.currency("IDR")
			.transactOn(Instant.ofEpochMilli(1706617931617L))
			.transactMethod("alfamart")
			.customerInfo(customerInfo)
			.items(List.of(
				item1
			))
			.invoice(invoice)
			.build();


		assertDoesNotThrow(() -> {
			entityManager.persistAndFlush(customerInfo);
			entityManager.persistAndFlush(item1);

			assertThat(entityManager.getEntityManager().contains(customerInfo)).isTrue();
			assertThat(entityManager.getEntityManager().contains(item1)).isTrue();

			TransactionEntity savedTransaction = entityManager.persistAndFlush(transactionData);
			assertThat(savedTransaction)
				.extracting("transactId", "grossAmount", "currency", "orderId", "customerInfo.userId", "invoice.store", "invoice.paymentCode")
				.contains("2323", 20_000d, "IDR", "order-1", "2434", "alfamart", "123456789");

			assertThat(savedTransaction).extracting(TransactionEntity::getItems).asList().hasSize(1).containsOnly(item1);
			assertThat(savedTransaction.getInvoice()).isInstanceOf(CStoreInvoiceEntity.class);

		});

	}

	@Test
	@Order(1)
	void shouldSavedNewTransaction() {
		CustomerInfoEntity customerInfo = CustomerInfoEntity.builder()
			.userId("2434")
			.username("joko")
			.build();
		ItemEntity item1 = ItemEntity.builder().itemId("3242")
			.price(10000)
			.quantity(2)
			.itemName("faker boom")
			.build();
		TransactionEntity transactionData = TransactionEntity.builder()
			.transactId("2121")
			.grossAmount(20000)
			.orderId("order-1")
			.currency("IDR")
			.transactOn(Instant.ofEpochMilli(1706617931617L))
			.transactMethod("cash")
			.customerInfo(customerInfo)
			.items(List.of(
				item1
			))
			.build();


		assertDoesNotThrow(() -> {
			TransactionEntity savedTransaction = entityManager.persistAndFlush(transactionData);
			assertThat(savedTransaction).extracting(TransactionEntity::getCustomerInfo).extracting(CustomerInfoEntity::getUserId)
				.isNotNull().isEqualTo(transactionData.getCustomerInfo().getUserId());

			assertThat(savedTransaction).extracting(TransactionEntity::getItems).asList().hasSize(1).containsOnly(item1);
		});

	}
}