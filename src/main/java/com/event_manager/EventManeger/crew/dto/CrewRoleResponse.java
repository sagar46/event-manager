package com.event_manager.EventManeger.crew.dto;

import java.util.Set;

import com.event_manager.EventManeger.crew.CrewCapability;

public record CrewRoleResponse(
		Long id,
		String code,
		String name,
		String description,
		Set<CrewCapability> capabilities) {
}
