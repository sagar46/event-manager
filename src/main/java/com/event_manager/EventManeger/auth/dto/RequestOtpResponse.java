package com.event_manager.EventManeger.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RequestOtpResponse(
		String phone,
		long expiresInSeconds,
		String devOtp) {
}
