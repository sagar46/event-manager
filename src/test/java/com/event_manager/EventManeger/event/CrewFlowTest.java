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
class CrewFlowTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JsonMapper jsonMapper;

	@Test
	void crewAppliesAndContributorApproves() throws Exception {
		register("9711111111", "ORGANIZER");
		register("9722222222", "CONTRIBUTOR");
		long contributorId = currentUserId(login("9722222222"));
		registerCrew("9733333333", contributorId);

		String organizerToken = login("9711111111");
		String contributorToken = login("9722222222");
		String crewToken = login("9733333333");
		String adminToken = login("9999999999");

		updateCity(contributorToken, "Pune");
		updateCity(crewToken, "Pune");

		MvcResult createResult = mockMvc.perform(post("/api/organizer/events")
						.header("Authorization", "Bearer " + organizerToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "Pune Crew Night",
								  "description": "Need on-site crew",
								  "location": "Pune",
								  "taggedContributorIds": [%s]
								}
								""".formatted(contributorId)))
				.andExpect(status().isCreated())
				.andReturn();

		long eventId = jsonMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

		mockMvc.perform(post("/api/admin/events/" + eventId + "/approve")
						.header("Authorization", "Bearer " + adminToken))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/crew/events")
						.header("Authorization", "Bearer " + crewToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(eventId));

		mockMvc.perform(post("/api/crew/events/" + eventId + "/applications")
						.header("Authorization", "Bearer " + crewToken))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.myCrewApplication.status").value("PENDING"));

		MvcResult pendingResult = mockMvc.perform(get("/api/contributor/crew-applications")
						.header("Authorization", "Bearer " + contributorToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].crew.phone").value("9733333333"))
				.andReturn();

		long applicationId = jsonMapper.readTree(pendingResult.getResponse().getContentAsString()).get(0).get("id").asLong();

		mockMvc.perform(post("/api/contributor/crew-applications/" + applicationId + "/approve")
						.header("Authorization", "Bearer " + contributorToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("APPROVED"));
	}

	private void register(String phone, String role) throws Exception {
		register(phone, role, null);
	}

	private void registerCrew(String phone, long contributorId) throws Exception {
		register(phone, "CREW", contributorId);
	}

	private void register(String phone, String role, Long contributorId) throws Exception {
		String otp = requestOtp(phone, "REGISTER");
		String contributorField = contributorId == null
				? ""
				: ",\n  \"affiliatedContributorId\": %d".formatted(contributorId);
		mockMvc.perform(post("/api/auth/otp/verify")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "phone": "%s",
								  "otp": "%s",
								  "purpose": "REGISTER",
								  "fullName": "Test User",
								  "role": "%s"%s
								}
								""".formatted(phone, otp, role, contributorField)))
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
