package com.event_manager.EventManeger.catalog;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.event_manager.EventManeger.crew.CrewCapability;
import com.event_manager.EventManeger.crew.CrewRole;
import com.event_manager.EventManeger.crew.CrewRoleRepository;
import com.event_manager.EventManeger.form.Form;
import com.event_manager.EventManeger.form.FormQuestion;
import com.event_manager.EventManeger.form.FormQuestionRepository;
import com.event_manager.EventManeger.form.FormRepository;
import com.event_manager.EventManeger.form.QuestionType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Order(20)
@RequiredArgsConstructor
@Slf4j
public class CatalogSeedRunner implements ApplicationRunner {

	private final EventCategoryRepository categoryRepository;
	private final EventTypeRepository typeRepository;
	private final CrewRoleRepository crewRoleRepository;
	private final FormRepository formRepository;
	private final FormQuestionRepository formQuestionRepository;

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		seedCategoriesAndTypes();
		seedCrewRoles();
		seedPulseSamplingForm();
		log.info("Catalog seed completed");
	}

	private void seedCategoriesAndTypes() {
		record TypeDef(String code, String name) {
		}
		Map<String, String> categoryNames = Map.of(
				"ENTERTAINMENT", "Entertainment",
				"SAMPLING", "Sampling",
				"CORPORATE", "Corporate",
				"PROMOTIONAL", "Promotional",
				"SPORTS", "Sports",
				"EXHIBITION", "Exhibition",
				"WEDDING", "Wedding",
				"COMMUNITY", "Community");

		Map<String, List<TypeDef>> taxonomy = new java.util.LinkedHashMap<>();
		taxonomy.put("ENTERTAINMENT", List.of(
				new TypeDef("CONCERT", "Concert"),
				new TypeDef("AWARD_SHOW", "Award Show"),
				new TypeDef("LIVE_PERFORMANCE", "Live Performance")));
		taxonomy.put("SAMPLING", List.of(
				new TypeDef("PRODUCT_SAMPLING", "Product Sampling"),
				new TypeDef("PULSE_SAMPLING", "Pulse Sampling"),
				new TypeDef("FOOD_SAMPLING", "Food Sampling")));
		taxonomy.put("CORPORATE", List.of(
				new TypeDef("CONFERENCE", "Conference"),
				new TypeDef("WORKSHOP", "Workshop"),
				new TypeDef("PRODUCT_LAUNCH", "Product Launch"),
				new TypeDef("SEMINAR", "Seminar")));
		taxonomy.put("PROMOTIONAL", List.of(new TypeDef("BRAND_ACTIVATION", "Brand Activation")));
		taxonomy.put("SPORTS", List.of(new TypeDef("MATCH", "Match")));
		taxonomy.put("EXHIBITION", List.of(new TypeDef("TRADE_SHOW", "Trade Show")));
		taxonomy.put("WEDDING", List.of(new TypeDef("CEREMONY", "Ceremony")));
		taxonomy.put("COMMUNITY", List.of(new TypeDef("FESTIVAL", "Festival")));

		for (var entry : taxonomy.entrySet()) {
			EventCategory category = categoryRepository.findByCode(entry.getKey()).orElseGet(() -> {
				EventCategory created = new EventCategory();
				created.setCode(entry.getKey());
				created.setName(categoryNames.get(entry.getKey()));
				return categoryRepository.save(created);
			});
			for (TypeDef typeDef : entry.getValue()) {
				typeRepository.findByCategoryIdAndCode(category.getId(), typeDef.code()).orElseGet(() -> {
					EventType type = new EventType();
					type.setCategory(category);
					type.setCode(typeDef.code());
					type.setName(typeDef.name());
					return typeRepository.save(type);
				});
			}
		}
	}

	private void seedCrewRoles() {
		ensureRole("PHOTOGRAPHER", "Photographer", Set.of(CrewCapability.UPLOAD_MEDIA));
		ensureRole("VIDEOGRAPHER", "Videographer", Set.of(CrewCapability.UPLOAD_MEDIA));
		ensureRole("DECORATOR", "Decorator", Set.of());
		ensureRole("WATERBOY", "Waterboy", Set.of());
		ensureRole("BODYBUILDER", "Bodybuilder", Set.of());
		ensureRole("PROMOTER", "Promoter", Set.of());
		ensureRole(
				"SUPERVISOR",
				"Supervisor",
				Set.of(
						CrewCapability.VIEW_EVENT_CREW,
						CrewCapability.VIEW_ASSIGNMENTS,
						CrewCapability.MARK_ATTENDANCE,
						CrewCapability.UPLOAD_MEDIA,
						CrewCapability.MONITOR_ACTIVITIES,
						CrewCapability.REVIEW_FEEDBACK));
		ensureRole(
				"MANAGER",
				"Manager",
				Set.of(
						CrewCapability.VIEW_EVENT_CREW,
						CrewCapability.VIEW_ASSIGNMENTS,
						CrewCapability.MARK_ATTENDANCE,
						CrewCapability.CORRECT_ATTENDANCE,
						CrewCapability.UPLOAD_MEDIA,
						CrewCapability.SUBMIT_REPORT,
						CrewCapability.REVIEW_FEEDBACK,
						CrewCapability.MONITOR_ACTIVITIES));
	}

	private void ensureRole(String code, String name, Set<CrewCapability> capabilities) {
		crewRoleRepository.findByCode(code).orElseGet(() -> {
			CrewRole role = new CrewRole();
			role.setCode(code);
			role.setName(name);
			role.setCapabilities(capabilities);
			return crewRoleRepository.save(role);
		});
	}

	private void seedPulseSamplingForm() {
		if (formRepository.findByActiveTrueOrderByNameAsc().stream()
				.anyMatch(form -> form.getName().equalsIgnoreCase("Pulse Sampling Feedback"))) {
			return;
		}
		Form form = new Form();
		form.setName("Pulse Sampling Feedback");
		form.setDescription("Customer feedback for Pulse sampling events");
		form = formRepository.save(form);

		saveQuestion(form, "Have you heard of Pulse before?", QuestionType.YES_NO, 1);
		saveQuestion(form, "Did you like the product?", QuestionType.RATING, 2);
		saveQuestion(form, "Would you purchase the product?", QuestionType.YES_NO, 3);
		saveQuestion(form, "Additional feedback?", QuestionType.TEXT, 4);
	}

	private void saveQuestion(Form form, String prompt, QuestionType type, int order) {
		FormQuestion question = new FormQuestion();
		question.setForm(form);
		question.setPrompt(prompt);
		question.setQuestionType(type);
		question.setRequired(type != QuestionType.TEXT);
		question.setSortOrder(order);
		formQuestionRepository.save(question);
	}
}
