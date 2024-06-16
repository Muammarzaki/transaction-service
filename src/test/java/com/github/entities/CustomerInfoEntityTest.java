package com.github.entities;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("integration-testing")
@DataJpaTest
@ActiveProfiles("test")
@Sql({"classpath:scheme/customer_entity_init.sql"})
class CustomerInfoEntityTest {
	@Autowired
	TestEntityManager entityManager;

	@Test
	void shouldSaveToDatabases() {
		String username = "joko";
		String userId = UUID.randomUUID().toString();

		CustomerInfoEntity customerInfo = CustomerInfoEntity.builder().userId(userId).username(username).build();

		CustomerInfoEntity withNewId = entityManager.persistFlushFind(customerInfo);

		assertThat(withNewId).extracting(CustomerInfoEntity::getUserId).isNotNull();
	}

	@Test
	void checkIsUserWithSameUserIDShouldNotDuplicate() {
		String username = "joko";
		String userId = "1111";

		CustomerInfoEntity customerInfo = CustomerInfoEntity.builder().userId(userId).username(username).build();

		assertThrows(ConstraintViolationException.class, () ->
			entityManager.persistFlushFind(customerInfo)
		);
	}

}