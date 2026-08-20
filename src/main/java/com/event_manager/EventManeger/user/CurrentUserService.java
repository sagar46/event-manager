package com.event_manager.EventManeger.user;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.event_manager.EventManeger.auth.PhoneNumbers;
import com.event_manager.EventManeger.common.NotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

	private final UserRepository userRepository;

	public User require(Authentication authentication) {
		String phone = PhoneNumbers.normalize(authentication.getName());
		return userRepository.findByPhone(phone)
				.orElseThrow(() -> new NotFoundException("User not found"));
	}
}
