package com.event_manager.EventManeger.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JsonMapper jsonMapper;

	@Test
	void adminLoginAndReadCurrentUser() throws Exception {
		String token = login("9999999999");

		mockMvc.perform(get("/api/auth/me")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.phone").value("9999999999"))
				.andExpect(jsonPath("$.email").value("admin@eventmanager.local"))
				.andExpect(jsonPath("$.roles[0]").value("ADMIN"));
	}

	@Test
	void protectedEndpointRequiresAuthentication() throws Exception {
		mockMvc.perform(get("/api/auth/me"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void organizerContributorAndCrewCanRegisterAndLogin() throws Exception {
		registerAndLogin("9000000001", "ORGANIZER");
		registerAndLogin("9000000002", "CONTRIBUTOR");
		long contributorId = currentUserId(login("9000000002"));
		registerCrewAndLogin("9000000003", contributorId);
	}

	@Test
	void cannotRegisterAsAdmin() throws Exception {
		mockMvc.perform(post("/api/auth/otp/verify")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "phone": "9000000004",
								  "otp": "123456",
								  "purpose": "REGISTER",
								  "fullName": "Fake Admin",
								  "role": "ADMIN"
								}
								"""))
				.andExpect(status().isBadRequest());
	}

	@Test
	void adminCanCreateOtherPersonas() throws Exception {
		String adminToken = login("9999999999");

		mockMvc.perform(post("/api/admin/users")
						.header("Authorization", "Bearer " + adminToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "fullName": "Omar Organizer",
								  "phone": "9111111111",
								  "role": "ORGANIZER"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.phone").value("9111111111"))
				.andExpect(jsonPath("$.roles[0]").value("ORGANIZER"));
	}

	@Test
	void duplicatePhoneIsRejected() throws Exception {
		String adminToken = login("9999999999");
		String body = """
				{
				  "fullName": "Casey Contributor",
				  "phone": "9222222222",
				  "role": "CONTRIBUTOR"
				}
				""";

		mockMvc.perform(post("/api/admin/users")
						.header("Authorization", "Bearer " + adminToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/admin/users")
						.header("Authorization", "Bearer " + adminToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isConflict());
	}

	@Test
	void unknownPhoneCannotRequestLoginOtp() throws Exception {
		mockMvc.perform(post("/api/auth/otp/request")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "phone": "9888888888",
								  "purpose": "LOGIN"
								}
								"""))
				.andExpect(status().isNotFound());
	}

	@Test
	void invalidPhoneIsRejected() throws Exception {
		String adminToken = login("9999999999");

		mockMvc.perform(post("/api/admin/users")
						.header("Authorization", "Bearer " + adminToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "fullName": "Bad Phone",
								  "phone": "12345",
								  "role": "CREW"
								}
								"""))
				.andExpect(status().isBadRequest());
	}

	@Test
	void adminCannotCreateAnotherAdminThroughApi() throws Exception {
		String adminToken = login("9999999999");

		mockMvc.perform(post("/api/admin/users")
						.header("Authorization", "Bearer " + adminToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "fullName": "Second Admin",
								  "phone": "9333333333",
								  "role": "ADMIN"
								}
								"""))
				.andExpect(status().isBadRequest());
	}

	@Test
	void adminCanAccessAdminRoutes() throws Exception {
		String token = login("9999999999");

		mockMvc.perform(get("/api/admin/ping")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isNotFound());
	}

	@Test
	void crewCannotAccessAdminRoutes() throws Exception {
		registerAndLogin("9555555555", "CONTRIBUTOR");
		long contributorId = currentUserId(login("9555555555"));
		registerCrewAndLogin("9444444444", contributorId);
		String crewToken = login("9444444444");

		mockMvc.perform(get("/api/admin/ping")
						.header("Authorization", "Bearer " + crewToken))
				.andExpect(status().isForbidden());
	}

	private void registerCrewAndLogin(String phone, long contributorId) throws Exception {
		register(phone, "CREW", contributorId);
		login(phone);
	}

	private void registerAndLogin(String phone, String role) throws Exception {
		register(phone, role, null);
		login(phone);
	}

	private void register(String phone, String role) throws Exception {
		register(phone, role, null);
	}

	private void register(String phone, String role, Long affiliatedContributorId) throws Exception {
		String otp = requestOtp(phone, "REGISTER");
		String contributorField = affiliatedContributorId == null
				? ""
				: ",\n  \"affiliatedContributorId\": %d".formatted(affiliatedContributorId);
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
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.accessToken").isNotEmpty())
				.andExpect(jsonPath("$.user.phone").value(phone))
				.andExpect(jsonPath("$.user.roles[0]").value(role))
				.andExpect(jsonPath("$.user.profileCompletion.percent").value(33))
				.andExpect(jsonPath("$.user.profileCompletion.complete").value(false));
	}

	private long currentUserId(String token) throws Exception {
		MvcResult result = mockMvc.perform(get("/api/auth/me")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andReturn();
		return jsonMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
	}

	private String login(String phone) throws Exception {
		String otp = requestOtp(phone, "LOGIN");
		MvcResult loginResult = mockMvc.perform(post("/api/auth/otp/verify")
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

		JsonNode body = jsonMapper.readTree(loginResult.getResponse().getContentAsString());
		return body.get("accessToken").asText();
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
				.andExpect(jsonPath("$.devOtp").isNotEmpty())
				.andReturn();
		return jsonMapper.readTree(result.getResponse().getContentAsString()).get("devOtp").asText();
	}
}
