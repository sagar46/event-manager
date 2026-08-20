package com.event_manager.EventManeger.event.dto;

import jakarta.validation.constraints.NotNull;

public record TagContributorRequest(
		@NotNull(message = "Contributor id is required")
		Long contributorId) {
}
