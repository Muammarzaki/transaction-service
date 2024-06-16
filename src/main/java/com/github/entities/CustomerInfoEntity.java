package com.github.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "customer_info")
public class CustomerInfoEntity {
	@Id
	@Column(name = "user_id")
	String userId;
	@Column(name = "username", nullable = false)
	String username;

}
