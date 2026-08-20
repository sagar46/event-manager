package com.event_manager.EventManeger.auth.dto;

public record AuthResponse(
		String accessToken,
		String tokenType,
		long expiresInSeconds,
		UserResponse user) {
}
