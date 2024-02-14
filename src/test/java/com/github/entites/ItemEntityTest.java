package com.github.entites;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@DataJpaTest
@Tag("integration-testing")
@ActiveProfiles("test")
class ItemEntityTest {
	@Autowired
	TestEntityManager entityManager;

	@Test
	void shouldestSaveNewItem() {
		ItemEntity dataItem = ItemEntity.builder().ItemId("1234").itemName("chocolate")
			.price(20000).count(100).build();

		assertDoesNotThrow(() -> {
			ItemEntity savedItem = entityManager.persistAndFlush(dataItem);
			assertThat(savedItem).extracting(ItemEntity::getItemId).isNotNull();
		});
	}
}