package com.event_manager.EventManeger.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.event_manager.EventManeger.auth.PhoneNumbers;
import com.event_manager.EventManeger.common.InvalidPhoneException;
import com.event_manager.EventManeger.user.User;
import com.event_manager.EventManeger.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DatabaseUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		String phone;
		try {
			phone = PhoneNumbers.normalize(username);
		} catch (InvalidPhoneException exception) {
			throw new UsernameNotFoundException("User not found");
		}

		User user = userRepository.findByPhone(phone)
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));

		return org.springframework.security.core.userdetails.User.builder()
				.username(user.getPhone())
				.password(user.getPassword() == null || user.getPassword().isBlank() ? "{noop}otp" : user.getPassword())
				.disabled(!user.isEnabled())
				.authorities(user.getRoles().stream()
						.map(role -> "ROLE_" + role.name())
						.toArray(String[]::new))
				.build();
	}
}
