package com.event_manager.EventManeger.profile;

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
class ProfileControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JsonMapper jsonMapper;

	@Test
	void completingProfileReachesOneHundredPercent() throws Exception {
		String otp = requestOtp("9876543210", "REGISTER");
		MvcResult registerResult = mockMvc.perform(post("/api/auth/otp/verify")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "phone": "9876543210",
								  "otp": "%s",
								  "purpose": "REGISTER",
								  "fullName": "Priya Planner",
								  "role": "ORGANIZER"
								}
								""".formatted(otp)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.user.profileCompletion.percent").value(33))
				.andExpect(jsonPath("$.user.profileCompletion.complete").value(false))
				.andExpect(jsonPath("$.user.profileCompletion.missingFields.length()").value(4))
				.andReturn();

		String token = jsonMapper.readTree(registerResult.getResponse().getContentAsString())
				.get("accessToken")
				.asText();

		mockMvc.perform(patch("/api/profile")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "fullName": "Priya Planner",
								  "email": "priya.profile@example.com",
								  "city": "Pune",
								  "bio": "Plans live events and vendor schedules.",
								  "organization": "Northstage Events"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.profileCompletion.percent").value(100))
				.andExpect(jsonPath("$.profileCompletion.complete").value(true))
				.andExpect(jsonPath("$.city").value("Pune"))
				.andExpect(jsonPath("$.phone").value("9876543210"));
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
}
