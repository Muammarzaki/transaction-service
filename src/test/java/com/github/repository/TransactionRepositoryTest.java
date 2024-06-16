package com.github.repository;

import com.github.entities.TransactionEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@ActiveProfiles("test")
@Sql("classpath:scheme/transaction_entity_init.sql")
@DataJpaTest
class TransactionRepositoryTest {
	@Autowired
	TransactionRepository repository;
	@Autowired
	TestEntityManager testEntityManager;

	@Test
	void testFindByOrderId() {
		Optional<TransactionEntity> byOrderId = repository.findByOrderId("order-1");
		assertThat(byOrderId).isPresent();
		TransactionEntity transactionEntity = byOrderId.get();
		assertThat(transactionEntity)
			.extracting("customerInfo.userId", "orderId", "transactId", "transactFinishOn", "transactStatus")
			.contains("1234", "order-1", "12345", null, "pending");
		assertThat(transactionEntity.getItems())
			.extracting("itemId", "itemName")
			.containsAnyOf(tuple("1234", "mie bakso"));

	}

	@Test
	void testUpdateTransactionByOrderId() {
		LocalDateTime finishOn = LocalDateTime.of(2021, 12, 5, 14, 30, 15);
		int cancel = repository.updateTransaction("order-2", "cancel", finishOn.toInstant(ZoneOffset.UTC));
		assertThat(cancel).isPositive();

		TransactionEntity findTransaction = testEntityManager.find(TransactionEntity.class, "order-2");
		assertThat(findTransaction)
			.extracting("customerInfo.userId", "orderId", "transactId", "transactFinishOn", "transactStatus")
			.contains("1234", "order-2", "54321", finishOn.toInstant(ZoneOffset.UTC), "cancel");

		assertThat(findTransaction.getItems())
			.extracting("itemId", "itemName")
			.containsAnyOf(tuple("5432", "mie goreng"));
	}
}