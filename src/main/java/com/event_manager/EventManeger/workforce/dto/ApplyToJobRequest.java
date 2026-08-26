package com.event_manager.EventManeger.workforce.dto;

import jakarta.validation.constraints.Size;

public record ApplyToJobRequest(@Size(max = 500) String note) {
}
