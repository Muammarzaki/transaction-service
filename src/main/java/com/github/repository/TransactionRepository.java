package com.github.repository;

import com.github.entites.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, String> {
	public Optional<TransactionEntity> findByOrderId(String orderId);
}
