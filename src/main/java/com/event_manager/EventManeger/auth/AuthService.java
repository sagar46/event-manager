package com.event_manager.EventManeger.auth;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.event_manager.EventManeger.auth.dto.AuthResponse;
import com.event_manager.EventManeger.auth.dto.CreateUserRequest;
import com.event_manager.EventManeger.auth.dto.RequestOtpRequest;
import com.event_manager.EventManeger.auth.dto.RequestOtpResponse;
import com.event_manager.EventManeger.auth.dto.UserResponse;
import com.event_manager.EventManeger.auth.dto.VerifyOtpRequest;
import com.event_manager.EventManeger.common.InvalidPersonaException;
import com.event_manager.EventManeger.common.NotFoundException;
import com.event_manager.EventManeger.common.PhoneAlreadyExistsException;
import com.event_manager.EventManeger.security.JwtService;
import com.event_manager.EventManeger.user.Role;
import com.event_manager.EventManeger.user.User;
import com.event_manager.EventManeger.user.UserMapper;
import com.event_manager.EventManeger.user.UserRepository;
import com.event_manager.EventManeger.user.UserSummary;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private static final Set<Role> SELF_SERVICE_ROLES = EnumSet.of(Role.ORGANIZER, Role.CONTRIBUTOR, Role.CREW);

	private final UserRepository userRepository;
	private final OtpService otpService;
	private final JwtService jwtService;
	private final UserMapper userMapper;

	@Transactional
	public RequestOtpResponse requestOtp(RequestOtpRequest request) {
		String phone = PhoneNumbers.normalize(request.phone());
		boolean exists = userRepository.existsByPhone(phone);
		if (request.purpose() == OtpPurpose.LOGIN && !exists) {
			throw new NotFoundException("No account found for this mobile number. Please sign up");
		}
		if (request.purpose() == OtpPurpose.REGISTER && exists) {
			throw new PhoneAlreadyExistsException(phone);
		}
		return otpService.request(phone, request.purpose());
	}

	@Transactional
	public AuthResponse verifyOtp(VerifyOtpRequest request) {
		String phone = PhoneNumbers.normalize(request.phone());

		if (request.purpose() == OtpPurpose.REGISTER) {
			validateSelfServiceSignup(request.fullName(), request.role());
			otpService.verify(phone, request.otp(), request.purpose());
			return toAuthResponse(createPersonaUser(request.fullName(), phone, request.role(), request.affiliatedContributorId()));
		}

		otpService.verify(phone, request.otp(), request.purpose());
		User user = userRepository.findByPhone(phone)
				.orElseThrow(() -> new NotFoundException("No account found for this mobile number. Please sign up"));
		return toAuthResponse(user);
	}

	@Transactional
	public UserResponse createUser(CreateUserRequest request) {
		return userMapper.toResponse(createPersonaUser(
				request.fullName(),
				request.phone(),
				request.role(),
				request.affiliatedContributorId()));
	}

	@Transactional(readOnly = true)
	public UserResponse currentUser(String phone) {
		User user = userRepository.findByPhone(PhoneNumbers.normalize(phone))
				.orElseThrow(() -> new IllegalStateException("Authenticated user was not found"));
		return userMapper.toResponse(user);
	}

	@Transactional(readOnly = true)
	public List<UserSummary> listContributorsForSignup() {
		return userRepository.findAllByRole(Role.CONTRIBUTOR).stream()
				.map(UserSummary::from)
				.toList();
	}

	private void validateSelfServiceSignup(String fullName, Role role) {
		if (role == null || !SELF_SERVICE_ROLES.contains(role)) {
			throw new InvalidPersonaException(
					"Only ORGANIZER, CONTRIBUTOR, or CREW can sign up. Admin is a default system user");
		}
		if (fullName == null || fullName.trim().length() < 2) {
			throw new InvalidPersonaException("Full name is required to create an account");
		}
	}

	private User createPersonaUser(String fullName, String rawPhone, Role role, Long affiliatedContributorId) {
		validateSelfServiceSignup(fullName, role);

		String phone = PhoneNumbers.normalize(rawPhone);
		if (userRepository.existsByPhone(phone)) {
			throw new PhoneAlreadyExistsException(phone);
		}

		User user = new User();
		user.setFullName(fullName.trim());
		user.setPhone(phone);
		user.setEnabled(true);
		user.setRoles(Set.of(role));
		if (role == Role.CREW && affiliatedContributorId != null) {
			user.setAffiliatedContributor(requireContributor(affiliatedContributorId));
		}
		return userRepository.save(user);
	}

	private User requireContributor(Long contributorId) {
		return userRepository.findByIdAndRole(contributorId, Role.CONTRIBUTOR)
				.orElseThrow(() -> new NotFoundException("Contributor not found"));
	}

	private AuthResponse toAuthResponse(User user) {
		return new AuthResponse(
				jwtService.generateToken(user),
				"Bearer",
				jwtService.expiresInSeconds(),
				userMapper.toResponse(user));
	}

	public static String normalizeEmail(String email) {
		if (email == null || email.isBlank()) {
			return null;
		}
		return email.trim().toLowerCase(Locale.ROOT);
	}
}
