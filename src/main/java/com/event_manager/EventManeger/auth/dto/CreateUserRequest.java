package com.event_manager.EventManeger.auth.dto;

import com.event_manager.EventManeger.user.Role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
		@NotBlank(message = "Full name is required")
		@Size(min = 2, max = 80, message = "Full name must be between 2 and 80 characters")
		String fullName,

		@NotBlank(message = "Mobile number is required")
		String phone,

		@NotNull(message = "Role is required")
		Role role,

		Long affiliatedContributorId) {
}
