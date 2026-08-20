package com.event_manager.EventManeger.auth.dto;

import com.event_manager.EventManeger.auth.OtpPurpose;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RequestOtpRequest(
		@NotBlank(message = "Mobile number is required")
		String phone,

		@NotNull(message = "Purpose is required")
		OtpPurpose purpose) {
}
