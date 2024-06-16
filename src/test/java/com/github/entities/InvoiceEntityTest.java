package com.github.entities;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Tag("integration-testing")
@ActiveProfiles("test")
class InvoiceEntityTest {
	@Autowired
	TestEntityManager testEntityManager;

	@Test
	void shouldSaveCStoreInvoiceEntity() {
		CStoreInvoiceEntity entity = CStoreInvoiceEntity.builder()
			.store("alfamart")
			.paymentCode("123456789")
			.build();

		testEntityManager.persistAndFlush(entity);
		InvoiceEntity findEntity = testEntityManager.find(InvoiceEntity.class, entity.getId());
		assertThat(findEntity).isInstanceOf(CStoreInvoiceEntity.class)
			.extracting("id", "paymentCode")
			.contains(entity.getId(), entity.getPaymentCode());
	}

	@Test
	void shouldSaveBankInvoiceEntity() {
		BankTransferInvoiceEntity entity = BankTransferInvoiceEntity.builder()
			.bank("bsi")
			.vaNumber("123456789")
			.build();

		testEntityManager.persistAndFlush(entity);
		InvoiceEntity findEntity = testEntityManager.find(InvoiceEntity.class, entity.getId());
		assertThat(findEntity).isInstanceOf(BankTransferInvoiceEntity.class)
			.extracting("id", "vaNumber", "bank")
			.contains(entity.getId(), entity.getVaNumber(), "bsi");
	}

	@Test
	void shouldSaveEWalletInvoiceEntity() {
		EWalletInvoiceEntity entity = EWalletInvoiceEntity.builder()
			.url("https://localhost/qr-code")
			.name("qris-qrcode")
			.method("get")
			.build();

		testEntityManager.persistAndFlush(entity);
		InvoiceEntity findEntity = testEntityManager.find(InvoiceEntity.class, entity.getId());
		assertThat(findEntity).isInstanceOf(EWalletInvoiceEntity.class)
			.extracting("id", "name", "method", "url")
			.contains(entity.getId(), entity.getName(), entity.getMethod(), entity.getUrl());
	}
}