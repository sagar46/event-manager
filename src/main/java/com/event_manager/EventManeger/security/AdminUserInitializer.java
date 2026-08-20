package com.event_manager.EventManeger.security;

import java.util.Set;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.event_manager.EventManeger.auth.AuthService;
import com.event_manager.EventManeger.auth.PhoneNumbers;
import com.event_manager.EventManeger.user.Role;
import com.event_manager.EventManeger.user.User;
import com.event_manager.EventManeger.user.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminUserInitializer implements ApplicationRunner {

	private final UserRepository userRepository;
	private final AdminProperties adminProperties;

	@Override
	public void run(ApplicationArguments args) {
		String phone = PhoneNumbers.normalize(adminProperties.getPhone());
		String email = AuthService.normalizeEmail(adminProperties.getEmail());

		// Prefer the admin record identified by phone (OTP identity).
		if (userRepository.existsByPhone(phone)) {
			log.info("Default admin already exists by phone {}", phone);
			return;
		}

		// If an older run seeded by email (legacy schema), reuse that user row.
		if (email != null) {
			userRepository.findByEmail(email).ifPresent(existing -> {
				existing.setPhone(phone);
				existing.setEnabled(true);
				existing.setFullName(adminProperties.getFullName());
				existing.setRoles(Set.of(Role.ADMIN));
				// password remains NULL for OTP-only accounts
				userRepository.save(existing);
				log.info("Upgraded existing admin user {} -> {}", email, phone);
			});

			// If we successfully reused by email, stop.
			if (userRepository.existsByPhone(phone)) {
				return;
			}
		}

		User admin = new User();
		admin.setFullName(adminProperties.getFullName());
		admin.setPhone(phone);
		admin.setEmail(email);
		admin.setEnabled(true);
		admin.setRoles(Set.of(Role.ADMIN));
		userRepository.save(admin);

		log.info("Created default admin user {}", phone);
	}
}
