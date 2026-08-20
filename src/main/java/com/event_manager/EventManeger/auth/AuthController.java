package com.event_manager.EventManeger.auth;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.event_manager.EventManeger.auth.dto.AuthResponse;
import com.event_manager.EventManeger.auth.dto.RequestOtpRequest;
import com.event_manager.EventManeger.auth.dto.RequestOtpResponse;
import com.event_manager.EventManeger.auth.dto.UserResponse;
import com.event_manager.EventManeger.auth.dto.VerifyOtpRequest;
import com.event_manager.EventManeger.user.UserSummary;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@GetMapping("/contributors")
	public List<UserSummary> contributors() {
		return authService.listContributorsForSignup();
	}

	@PostMapping("/otp/request")
	public RequestOtpResponse requestOtp(@Valid @RequestBody RequestOtpRequest request) {
		return authService.requestOtp(request);
	}

	@PostMapping("/otp/verify")
	public ResponseEntity<AuthResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
		AuthResponse response = authService.verifyOtp(request);
		HttpStatus status = request.purpose() == OtpPurpose.REGISTER ? HttpStatus.CREATED : HttpStatus.OK;
		return ResponseEntity.status(status).body(response);
	}

	@GetMapping("/me")
	public UserResponse me(Authentication authentication) {
		return authService.currentUser(authentication.getName());
	}
}
