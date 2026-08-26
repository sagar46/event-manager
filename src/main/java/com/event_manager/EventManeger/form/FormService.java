package com.event_manager.EventManeger.form;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.event_manager.EventManeger.common.ConflictException;
import com.event_manager.EventManeger.common.NotFoundException;
import com.event_manager.EventManeger.event.Event;
import com.event_manager.EventManeger.form.dto.FormDetailResponse;
import com.event_manager.EventManeger.form.dto.FormQuestionResponse;
import com.event_manager.EventManeger.form.dto.SubmitFormAnswerRequest;
import com.event_manager.EventManeger.form.dto.SubmitFormResponseRequest;
import com.event_manager.EventManeger.user.User;
import com.event_manager.EventManeger.workforce.CrewAssignment;
import com.event_manager.EventManeger.workforce.CrewAssignmentRepository;
import com.event_manager.EventManeger.workforce.EventAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FormService {

	private final FormRepository formRepository;
	private final FormQuestionRepository questionRepository;
	private final FormResponseRepository responseRepository;
	private final FormAnswerRepository answerRepository;
	private final EventAccessService eventAccessService;
	private final CrewAssignmentRepository assignmentRepository;

	@Transactional(readOnly = true)
	public FormDetailResponse getForm(Long formId) {
		Form form = formRepository.findById(formId)
				.orElseThrow(() -> new NotFoundException("Form not found"));
		List<FormQuestionResponse> questions = questionRepository.findByFormOrderBySortOrderAsc(form).stream()
				.map(question -> new FormQuestionResponse(
						question.getId(),
						question.getPrompt(),
						question.getQuestionType(),
						question.getOptionsJson(),
						question.isRequired(),
						question.getSortOrder()))
				.toList();
		return new FormDetailResponse(form.getId(), form.getName(), form.getDescription(), questions);
	}

	@Transactional
	public Long submit(User actor, Long eventId, SubmitFormResponseRequest request) {
		Event event = eventAccessService.requireEvent(eventId);
		Form form = formRepository.findById(request.formId())
				.orElseThrow(() -> new NotFoundException("Form not found"));
		if (event.getFeedbackForm() != null && !event.getFeedbackForm().getId().equals(form.getId())) {
			throw new ConflictException("This form is not linked to the event");
		}

		FormResponse response = new FormResponse();
		response.setForm(form);
		response.setEvent(event);
		response.setSubmittedBy(actor);
		if (request.assignmentId() != null) {
			CrewAssignment assignment = assignmentRepository.findById(request.assignmentId())
					.orElseThrow(() -> new NotFoundException("Assignment not found"));
			response.setAssignment(assignment);
		}
		response = responseRepository.save(response);

		List<FormQuestion> questions = questionRepository.findByFormOrderBySortOrderAsc(form);
		for (SubmitFormAnswerRequest answerRequest : request.answers()) {
			FormQuestion question = questions.stream()
					.filter(item -> item.getId().equals(answerRequest.questionId()))
					.findFirst()
					.orElseThrow(() -> new NotFoundException("Question not found on form"));
			FormAnswer answer = new FormAnswer();
			answer.setResponse(response);
			answer.setQuestion(question);
			answer.setValueText(answerRequest.valueText());
			answer.setValueNumber(answerRequest.valueNumber());
			answer.setValueBoolean(answerRequest.valueBoolean());
			answerRepository.save(answer);
		}
		return response.getId();
	}

	@Transactional(readOnly = true)
	public List<FormDetailResponse> listActiveForms() {
		List<FormDetailResponse> forms = new ArrayList<>();
		for (Form form : formRepository.findByActiveTrueOrderByNameAsc()) {
			forms.add(getForm(form.getId()));
		}
		return forms;
	}
}
