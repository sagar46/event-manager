package com.event_manager.EventManeger.attendance.dto;

import jakarta.validation.constraints.Size;

public record SelfCheckInRequest(
		Double latitude,
		Double longitude,

		@Size(max = 500)
		String remarks) {
}
