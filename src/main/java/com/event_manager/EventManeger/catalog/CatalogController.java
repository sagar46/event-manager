package com.event_manager.EventManeger.catalog;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.event_manager.EventManeger.catalog.dto.EventCategoryResponse;
import com.event_manager.EventManeger.catalog.dto.EventTypeResponse;
import com.event_manager.EventManeger.crew.CrewRoleRepository;
import com.event_manager.EventManeger.crew.dto.CrewRoleResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/catalog")
@RequiredArgsConstructor
public class CatalogController {

	private final EventCategoryRepository categoryRepository;
	private final EventTypeRepository typeRepository;
	private final CrewRoleRepository crewRoleRepository;

	@GetMapping("/categories")
	public List<EventCategoryResponse> categories() {
		return categoryRepository.findByActiveTrueOrderByNameAsc().stream()
				.map(category -> new EventCategoryResponse(
						category.getId(),
						category.getCode(),
						category.getName(),
						category.getDescription()))
				.toList();
	}

	@GetMapping("/categories/{categoryId}/types")
	public List<EventTypeResponse> types(@PathVariable Long categoryId) {
		return typeRepository.findByCategoryIdAndActiveTrueOrderByNameAsc(categoryId).stream()
				.map(type -> new EventTypeResponse(
						type.getId(),
						type.getCategory().getId(),
						type.getCode(),
						type.getName(),
						type.getDescription()))
				.toList();
	}

	@GetMapping("/crew-roles")
	public List<CrewRoleResponse> crewRoles() {
		return crewRoleRepository.findByActiveTrueOrderByNameAsc().stream()
				.map(role -> new CrewRoleResponse(
						role.getId(),
						role.getCode(),
						role.getName(),
						role.getDescription(),
						role.getCapabilities()))
				.toList();
	}
}
