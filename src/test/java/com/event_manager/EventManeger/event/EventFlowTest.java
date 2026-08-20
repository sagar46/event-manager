package com.event_manager.EventManeger.event;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc
class EventFlowTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JsonMapper jsonMapper;

	@Test
	void adminApprovalTaggingAndApplications() throws Exception {
		register("9811111111", "ORGANIZER");
		register("9822222222", "CONTRIBUTOR");
		register("9833333333", "CONTRIBUTOR");
		register("9844444444", "CONTRIBUTOR");

		String organizerToken = login("9811111111");
		String taggedToken = login("9822222222");
		String applicantToken = login("9833333333");
		String otherCityToken = login("9844444444");
		String adminToken = login("9999999999");

		updateCity(taggedToken, "Pune");
		updateCity(applicantToken, "Pune");
		updateCity(otherCityToken, "Mumbai");

		long taggedId = currentUserId(taggedToken);
		long applicantId = currentUserId(applicantToken);

		MvcResult createResult = mockMvc.perform(post("/api/organizer/events")
						.header("Authorization", "Bearer " + organizerToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "Pune Live Night",
								  "description": "Stage and vendor support",
								  "location": "Pune",
								  "taggedContributorIds": [%s]
								}
								""".formatted(taggedId)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value("PENDING_APPROVAL"))
				.andExpect(jsonPath("$.taggedContributors.length()").value(1))
				.andReturn();

		long eventId = jsonMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

		mockMvc.perform(get("/api/contributor/events")
						.header("Authorization", "Bearer " + applicantToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isEmpty());

		mockMvc.perform(post("/api/admin/events/" + eventId + "/approve")
						.header("Authorization", "Bearer " + adminToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("APPROVED"));

		mockMvc.perform(get("/api/contributor/events")
						.header("Authorization", "Bearer " + applicantToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(eventId))
				.andExpect(jsonPath("$[0].tagged").value(false));

		mockMvc.perform(get("/api/contributor/events")
						.header("Authorization", "Bearer " + otherCityToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isEmpty());

		mockMvc.perform(get("/api/contributor/events")
						.header("Authorization", "Bearer " + taggedToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].tagged").value(true));

		mockMvc.perform(post("/api/contributor/events/" + eventId + "/applications")
						.header("Authorization", "Bearer " + taggedToken))
				.andExpect(status().isConflict());

		MvcResult applyResult = mockMvc.perform(post("/api/contributor/events/" + eventId + "/applications")
						.header("Authorization", "Bearer " + applicantToken))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.myApplication.status").value("PENDING"))
				.andReturn();

		long applicationId = jsonMapper.readTree(applyResult.getResponse().getContentAsString())
				.get("myApplication")
				.get("id")
				.asLong();

		mockMvc.perform(post("/api/organizer/events/" + eventId + "/applications/" + applicationId + "/approve")
						.header("Authorization", "Bearer " + organizerToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.applications[0].contributor.id").value(applicantId))
				.andExpect(jsonPath("$.applications[0].status").value("APPROVED"));
	}

	private void register(String phone, String role) throws Exception {
		String otp = requestOtp(phone, "REGISTER");
		mockMvc.perform(post("/api/auth/otp/verify")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "phone": "%s",
								  "otp": "%s",
								  "purpose": "REGISTER",
								  "fullName": "Test User",
								  "role": "%s"
								}
								""".formatted(phone, otp, role)))
				.andExpect(status().isCreated());
	}

	private String login(String phone) throws Exception {
		String otp = requestOtp(phone, "LOGIN");
		MvcResult result = mockMvc.perform(post("/api/auth/otp/verify")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "phone": "%s",
								  "otp": "%s",
								  "purpose": "LOGIN"
								}
								""".formatted(phone, otp)))
				.andExpect(status().isOk())
				.andReturn();
		return jsonMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
	}

	private String requestOtp(String phone, String purpose) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/auth/otp/request")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "phone": "%s",
								  "purpose": "%s"
								}
								""".formatted(phone, purpose)))
				.andExpect(status().isOk())
				.andReturn();
		return jsonMapper.readTree(result.getResponse().getContentAsString()).get("devOtp").asText();
	}

	private void updateCity(String token, String city) throws Exception {
		mockMvc.perform(patch("/api/profile")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "fullName": "Test User",
								  "city": "%s"
								}
								""".formatted(city)))
				.andExpect(status().isOk());
	}

	private long currentUserId(String token) throws Exception {
		MvcResult result = mockMvc.perform(get("/api/auth/me")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andReturn();
		return jsonMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
	}
}
