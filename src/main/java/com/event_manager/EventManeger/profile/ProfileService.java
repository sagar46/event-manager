package com.event_manager.EventManeger.profile;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.event_manager.EventManeger.auth.AuthService;
import com.event_manager.EventManeger.auth.PhoneNumbers;
import com.event_manager.EventManeger.auth.dto.UpdateProfileRequest;
import com.event_manager.EventManeger.auth.dto.UserResponse;
import com.event_manager.EventManeger.common.EmailAlreadyExistsException;
import com.event_manager.EventManeger.common.NotFoundException;
import com.event_manager.EventManeger.user.Role;
import com.event_manager.EventManeger.user.User;
import com.event_manager.EventManeger.user.UserMapper;
import com.event_manager.EventManeger.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfileService {

	private final UserRepository userRepository;
	private final UserMapper userMapper;

	@Transactional
	public UserResponse update(String phone, UpdateProfileRequest request) {
		User user = userRepository.findByPhone(PhoneNumbers.normalize(phone))
				.orElseThrow(() -> new IllegalStateException("Authenticated user was not found"));

		String email = AuthService.normalizeEmail(request.email());
		if (email != null) {
			userRepository.findByEmail(email)
					.filter(existing -> !existing.getId().equals(user.getId()))
					.ifPresent(existing -> {
						throw new EmailAlreadyExistsException(email);
					});
		}

		user.setFullName(request.fullName().trim());
		user.setEmail(email);
		user.setCity(blankToNull(request.city()));
		user.setBio(blankToNull(request.bio()));
		user.setOrganization(blankToNull(request.organization()));
		if (user.getRoles().contains(Role.CREW) && request.affiliatedContributorId() != null) {
			user.setAffiliatedContributor(userRepository.findByIdAndRole(request.affiliatedContributorId(), Role.CONTRIBUTOR)
					.orElseThrow(() -> new NotFoundException("Contributor not found")));
		}

		return userMapper.toResponse(userRepository.save(user));
	}

	private String blankToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
