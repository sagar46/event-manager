package com.event_manager.EventManeger.catalog.dto;

public record EventTypeResponse(Long id, Long categoryId, String code, String name, String description) {
}
