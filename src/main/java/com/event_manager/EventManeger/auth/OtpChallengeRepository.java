package com.event_manager.EventManeger.auth;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OtpChallengeRepository extends JpaRepository<OtpChallenge, Long> {

	List<OtpChallenge> findByPhoneAndPurposeAndConsumedFalse(String phone, OtpPurpose purpose);

	Optional<OtpChallenge> findTopByPhoneAndPurposeAndConsumedFalseOrderByCreatedAtDesc(String phone, OtpPurpose purpose);

	Optional<OtpChallenge> findTopByPhoneAndPurposeOrderByCreatedAtDesc(String phone, OtpPurpose purpose);
}
