package com.event_manager.EventManeger.auth;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.event_manager.EventManeger.auth.dto.RequestOtpResponse;
import com.event_manager.EventManeger.common.ConflictException;
import com.event_manager.EventManeger.common.InvalidOtpException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

	private static final SecureRandom RANDOM = new SecureRandom();

	private final OtpChallengeRepository otpChallengeRepository;
	private final PasswordEncoder passwordEncoder;
	private final OtpProperties otpProperties;

	@Transactional
	public RequestOtpResponse request(String phone, OtpPurpose purpose) {
		otpChallengeRepository.findTopByPhoneAndPurposeOrderByCreatedAtDesc(phone, purpose)
				.filter(challenge -> challenge.getCreatedAt()
						.isAfter(Instant.now().minus(otpProperties.getResendCooldown())))
				.ifPresent(challenge -> {
					throw new ConflictException("Please wait a few seconds before requesting another OTP");
				});

		List<OtpChallenge> open = otpChallengeRepository.findByPhoneAndPurposeAndConsumedFalse(phone, purpose);
		open.forEach(challenge -> challenge.setConsumed(true));
		otpChallengeRepository.saveAll(open);

		String code = generateCode();
		OtpChallenge challenge = new OtpChallenge();
		challenge.setPhone(phone);
		challenge.setPurpose(purpose);
		challenge.setCodeHash(passwordEncoder.encode(code));
		challenge.setExpiresAt(Instant.now().plus(otpProperties.getTtl()));
		challenge.setCreatedAt(Instant.now());
		otpChallengeRepository.save(challenge);

		log.info("OTP generated for {} ({})", phone, purpose);
		return new RequestOtpResponse(
				phone,
				otpProperties.getTtl().toSeconds(),
				otpProperties.isDevEcho() ? code : null);
	}

	@Transactional
	public void verify(String phone, String otp, OtpPurpose purpose) {
		OtpChallenge challenge = otpChallengeRepository
				.findTopByPhoneAndPurposeAndConsumedFalseOrderByCreatedAtDesc(phone, purpose)
				.orElseThrow(() -> new InvalidOtpException("Request an OTP first"));

		if (challenge.getExpiresAt().isBefore(Instant.now())) {
			challenge.setConsumed(true);
			otpChallengeRepository.save(challenge);
			throw new InvalidOtpException("OTP has expired. Request a new one");
		}
		if (challenge.getAttempts() >= otpProperties.getMaxAttempts()) {
			challenge.setConsumed(true);
			otpChallengeRepository.save(challenge);
			throw new InvalidOtpException("Too many incorrect attempts. Request a new OTP");
		}

		challenge.setAttempts(challenge.getAttempts() + 1);
		if (!passwordEncoder.matches(otp.trim(), challenge.getCodeHash())) {
			otpChallengeRepository.save(challenge);
			throw new InvalidOtpException("Invalid OTP. Please try again");
		}

		challenge.setConsumed(true);
		otpChallengeRepository.save(challenge);
	}

	private String generateCode() {
		int bound = (int) Math.pow(10, otpProperties.getLength());
		int min = bound / 10;
		return String.format("%0" + otpProperties.getLength() + "d", RANDOM.nextInt(bound - min) + min);
	}
}
