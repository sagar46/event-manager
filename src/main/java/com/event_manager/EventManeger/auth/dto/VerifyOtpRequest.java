package com.event_manager.EventManeger.auth.dto;

import com.event_manager.EventManeger.auth.OtpPurpose;
import com.event_manager.EventManeger.user.Role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VerifyOtpRequest(
		@NotBlank(message = "Mobile number is required")
		String phone,

		@NotBlank(message = "OTP is required")
		@Size(min = 4, max = 8, message = "Enter the OTP sent to your mobile number")
		String otp,

		@NotNull(message = "Purpose is required")
		OtpPurpose purpose,

		String fullName,
		Role role,
		Long affiliatedContributorId) {
}
