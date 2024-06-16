package com.github.repository;

import com.github.entities.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, String> {
	public Optional<TransactionEntity> findByOrderId(String orderId);

	@Transactional
	@Modifying
	@Query("UPDATE com.github.entities.TransactionEntity t SET t.transactStatus = ?2 , t.transactFinishOn = ?3 WHERE t.orderId = ?1")
	public int updateTransaction(String orderId, String transactStatus, Instant transactFinishOn);
}
