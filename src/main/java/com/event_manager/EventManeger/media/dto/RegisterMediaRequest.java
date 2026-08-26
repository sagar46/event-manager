package com.event_manager.EventManeger.media.dto;

import com.event_manager.EventManeger.media.MediaType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterMediaRequest(
		@NotNull MediaType mediaType,
		@NotBlank @Size(max = 500) String storageKey,
		@Size(max = 255) String contentType,
		Long sizeBytes,
		@Size(max = 500) String caption,
		@Size(max = 120) String location,
		@Size(max = 2000) String metadataJson,
		Long assignmentId,
		Long activityId) {
}
