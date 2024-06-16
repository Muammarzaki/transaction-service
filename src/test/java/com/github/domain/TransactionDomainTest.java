package com.github.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.app.JacksonConfigurationTest;
import com.github.entities.CustomerInfoEntity;
import com.github.entities.ItemEntity;
import com.github.helpers.CustomerAdapter;
import com.github.helpers.ItemAdapter;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TransactionDomainTest {
	ObjectMapper mapper;
	Clock clock;
	DateTimeFormatter dateFormat;
	private Validator validator;

	@BeforeEach
	void setUp() {
		validator = Validation.buildDefaultValidatorFactory().getValidator();
		dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
		mapper = JacksonConfigurationTest.getConfig()
			.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		clock = Clock.fixed(Instant.now(), ZoneId.of("Asia/Jakarta"));
	}

	@Test
	void jsonShouldSerializeToPojo() {
		String paymentID = UUID.randomUUID().toString();
		LocalDateTime now = LocalDateTime.now(clock);

		LocalDateTime transactFinishOn = now.plusDays(2);
		String transactStatus = "done";
		Currency currency = Currency.getInstance("IDR");
		float mount = 60000;
		String method = "gopay";
		TransactionDomain.Response domain = TransactionDomain.Response.builder().orderId(paymentID).transactOn(now).transactFinishOn(transactFinishOn).transactStatus(transactStatus).items(new TransactionDomain.ItemsDomain()).customer(new TransactionDomain.CustomerDomain(null, null)).transactMethod(method).grossAmount(mount).currency(currency).build();
		String jsonFormat = """
			{
			    "order_id": "%s",
			    "transact_status": "%s",
			    "gross_amount": %f,
			    "currency": "%s",
			    "transact_on": "%s",
			    "transact_finish_on": "%s",
			    "transact_method": "%s",
			    "items": [],
			    "customer": {}}""".formatted(paymentID, transactStatus, mount, currency, now.format(dateFormat), transactFinishOn.format(dateFormat), method);

		Assertions.assertDoesNotThrow(() -> {
			JsonNode domainNode = mapper.readTree(mapper.writeValueAsString(domain));
			JsonNode jsonFormatNode = mapper.readTree(jsonFormat);
			assertEquals(jsonFormatNode, domainNode);
		});
	}

	@Test
	void itemsDomainShouldBeCreateAndCanSerialize() {
		TransactionDomain.ItemsDomain.ItemDomain itemDomain1 = new TransactionDomain.ItemsDomain.ItemDomain("barang-1", "indomie", 3, 3500.0);
		TransactionDomain.ItemsDomain.ItemDomain itemDomain2 = new TransactionDomain.ItemsDomain.ItemDomain("barang-2", "nutrisari", 2, 1000.0);
		TransactionDomain.ItemsDomain items = new TransactionDomain.ItemsDomain();
		items.add(itemDomain1);
		items.add(itemDomain2);

		assertThat(validator.validate(items)).isEmpty();

		assertDoesNotThrow(() -> {
			String json = mapper.writeValueAsString(items);
			assertThat(json)
				.contains("[", "]");
		});
	}

	@Test
	void shouldCreateItemsDomainWithDataIntegrityAndShouldBeConvertToItemEntity() {
		TransactionDomain.ItemsDomain.ItemDomain itemDomain1 = new TransactionDomain.ItemsDomain.ItemDomain("barang-1", "indomie", 3, 3_500.0);
		TransactionDomain.ItemsDomain.ItemDomain itemDomain2 = new TransactionDomain.ItemsDomain.ItemDomain("barang-2", "nutrisari", 2, 1_000.0);

		ItemEntity itemEntity1 = ItemAdapter.convertFromListOfItemEntityToItemEntity(itemDomain1);
		assertThat(itemEntity1)
			.extracting("itemId", "quantity").contains("barang-1", 3);
		assertThat(ItemAdapter.convertFromItemEntityToItemDomain(itemEntity1))
			.isEqualTo(itemDomain1)
			.extracting("itemId", "quantity", "price", "itemName")
			.contains("barang-1", 3, 3500.0, "indomie");

		TransactionDomain.ItemsDomain items = new TransactionDomain.ItemsDomain();
		items.add(itemDomain1);
		items.add(itemDomain2);

		assertThat(items).hasSize(2)
			.extracting("itemId", "itemName")
			.containsExactlyInAnyOrder(
				tuple("barang-1", "indomie"),
				tuple("barang-2", "nutrisari")
			);

		assertThat(items.getTotalPrice())
			.isEqualTo(12_500.0);

		List<ItemEntity> itemEntities = ItemAdapter.convertFromListOfItemEntityToItemEntity(items);
		assertThat(itemEntities)
			.extracting("itemId", "quantity").containsExactly(
				tuple("barang-1", 3),
				tuple("barang-2", 2)
			);

		TransactionDomain.ItemsDomain itemsDomainFromItemEntity = ItemAdapter.convertFromListOfItemEntityToItemsDomain(itemEntities);
		assertThat(itemsDomainFromItemEntity).isEqualTo(items);
	}

	@Test
	void createTransactionAndValidateValue() {
		TransactionDomain.ItemsDomain.ItemDomain itemDomain1 = new TransactionDomain.ItemsDomain.ItemDomain("barang-1", "indomie", 3, 3_500.0);
		TransactionDomain.ItemsDomain.ItemDomain itemDomain2 = new TransactionDomain.ItemsDomain.ItemDomain("barang-2", "nutrisari", 2, 1_000.0);
		TransactionDomain.ItemsDomain items = new TransactionDomain.ItemsDomain();
		items.add(itemDomain1);
		items.add(itemDomain2);
		TransactionDomain.CreateTransact createTransact = new TransactionDomain.CreateTransact(2000d, Currency.getInstance("IDR"), "alfamart", items, new TransactionDomain.CustomerDomain("ichikiwir", "joko"));
		assertThat(createTransact)
			.extracting("grossAmount", "currency", "transactMethod", "customer.username")
			.contains(2000.0, Currency.getInstance("IDR"), "alfamart", "joko");
	}

	@Test
	void shouldCreateNewCustomerDomain() {
		TransactionDomain.CustomerDomain customerDomain = new TransactionDomain.CustomerDomain("ichikiwir", "jonathan");
		assertThat(customerDomain)
			.extracting("userId", "username")
			.containsExactly("ichikiwir", "jonathan");

		CustomerInfoEntity customerInfoFromCustomerDomain = CustomerAdapter.convertToCustomerInfo(customerDomain);
		assertThat(customerInfoFromCustomerDomain)
			.extracting("userId", "username")
			.containsExactly(customerDomain.userId(), customerDomain.username());

		assertThat(CustomerAdapter.convertFromCustomerInfo(customerInfoFromCustomerDomain))
			.isEqualTo(customerDomain);

	}
}