package com.event_manager.EventManeger.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
		@NotBlank(message = "Full name is required")
		@Size(min = 2, max = 80, message = "Full name must be between 2 and 80 characters")
		String fullName,

		@Email(message = "Email must be valid")
		@Size(max = 191, message = "Email must be at most 191 characters")
		String email,

		@Size(max = 80, message = "City must be at most 80 characters")
		String city,

		@Size(max = 500, message = "Bio must be at most 500 characters")
		String bio,

		@Size(max = 120, message = "Organization must be at most 120 characters")
		String organization,

		Long affiliatedContributorId) {
}
