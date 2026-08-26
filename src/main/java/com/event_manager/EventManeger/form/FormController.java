package com.event_manager.EventManeger.form;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.event_manager.EventManeger.form.dto.FormDetailResponse;
import com.event_manager.EventManeger.form.dto.SubmitFormResponseRequest;
import com.event_manager.EventManeger.user.CurrentUserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FormController {

	private final FormService formService;
	private final CurrentUserService currentUserService;

	@GetMapping("/forms")
	public List<FormDetailResponse> listForms() {
		return formService.listActiveForms();
	}

	@GetMapping("/forms/{formId}")
	public FormDetailResponse getForm(@PathVariable Long formId) {
		return formService.getForm(formId);
	}

	@PostMapping("/events/{eventId}/form-responses")
	public Map<String, Long> submit(
			Authentication authentication,
			@PathVariable Long eventId,
			@Valid @RequestBody SubmitFormResponseRequest request) {
		Long id = formService.submit(currentUserService.require(authentication), eventId, request);
		return Map.of("responseId", id);
	}
}
