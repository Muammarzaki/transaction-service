package com.github.repository;

import com.github.entities.CustomerInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerInfoRepository extends JpaRepository<CustomerInfoEntity, String> {
}
