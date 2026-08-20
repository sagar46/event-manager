package com.event_manager.EventManeger.auth;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "otp_challenges")
@Getter
@Setter
@NoArgsConstructor
public class OtpChallenge {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 10)
	private String phone;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 16)
	private OtpPurpose purpose;

	@Column(nullable = false, length = 72)
	private String codeHash;

	@Column(nullable = false)
	private Instant expiresAt;

	@Column(nullable = false)
	private boolean consumed = false;

	@Column(nullable = false)
	private int attempts = 0;

	@Column(nullable = false, updatable = false)
	private Instant createdAt = Instant.now();
}
