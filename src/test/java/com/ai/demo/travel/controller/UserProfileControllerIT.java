package com.ai.demo.travel.controller;

import static com.ai.demo.travel.controller.TravelApiVersion.TRAVEL_ACCEPT_VERSION;
import static com.ai.demo.travel.controller.TravelApiVersion.TRAVEL_API_V1;
import static com.ai.demo.travel.controller.UserProfileController.ENDPOINT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ai.demo.BaseControllerIT;
import com.ai.demo.travel.dto.UserProfileDTO;
import com.ai.demo.travel.model.BudgetLevel;
import com.ai.demo.travel.model.Gender;
import com.ai.demo.travel.model.UserInterest;
import com.ai.demo.travel.model.UserProfile;
import com.ai.demo.travel.model.repository.UserProfileRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

class UserProfileControllerIT extends BaseControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        cleanup();
    }

    @AfterEach
    void cleanup() {
        userProfileRepository.deleteAll();
    }

    @Test
    void testCreateUserProfile() throws Exception {
        UserProfileDTO dto = createTestDto();

        mockMvc.perform(post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .header(TRAVEL_ACCEPT_VERSION, TRAVEL_API_V1)
                .content(asJsonString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.created_at").exists());

        List<UserProfile> all = userProfileRepository.findAll();
        assertThat(all).hasSize(1);
        UserProfile userProfile = all.get(0);

        assertThat(userProfile.getName()).isEqualTo("Alice");
        assertThat(userProfile.getBudgetLevel()).isEqualTo(BudgetLevel.MEDIUM);
        assertThat(userProfile.getGender()).isEqualTo(Gender.FEMALE);
        assertThat(userProfile.getBirth()).isEqualTo(LocalDate.of(1990, 5, 20));
        assertThat(userProfile.getPreferredClimates()).containsExactlyInAnyOrder("warm", "tropical");
        assertThat(userProfile.getLanguagesSpoken()).containsExactlyInAnyOrder("english", "french");
        assertThat(userProfile.getPassports()).containsExactlyInAnyOrder("BR", "ES");
        assertThat(userProfile.getTravelStyle()).isEqualTo("relaxed");
        assertThat(userProfile.getInterests().stream().map(UserInterest::getInterest).toList())
                .containsExactlyInAnyOrder("yoga", "local food", "hiking");
    }

    @Test
    void testCreateUserProfile_WithInvalidPassport() throws Exception {
        UserProfileDTO dto = createTestDto();
        String invalidCountry = "XPTO";
        dto.setPassports(List.of("BR", invalidCountry));

        mockMvc.perform(post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .header(TRAVEL_ACCEPT_VERSION, TRAVEL_API_V1)
                .content(asJsonString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("passports[1]: Invalid country code")));
    }

    @Test
    void testGetUserProfile() throws Exception {
        UserProfileDTO dto = createTestDto();

        String json = mockMvc.perform(post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .header(TRAVEL_ACCEPT_VERSION, TRAVEL_API_V1)
                .content(asJsonString(dto)))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(json).get("id").asLong();

        mockMvc.perform(get(ENDPOINT + "/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .header(TRAVEL_ACCEPT_VERSION, TRAVEL_API_V1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Alice"));
    }

    @Test
    void testUpdateUserProfile() throws Exception {
        UserProfileDTO dto = createTestDto();

        String json = mockMvc.perform(post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .header(TRAVEL_ACCEPT_VERSION, TRAVEL_API_V1)
                .content(asJsonString(dto)))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(json).get("id").asLong();

        UserProfileDTO updatedDto = UserProfileDTO.builder()
                .id(id)
                .name("Bob")
                .birth(LocalDate.of(1995, 1, 1))
                .gender(Gender.MALE)
                .budgetLevel(BudgetLevel.HIGH)
                .preferredClimates(List.of("tropical"))
                .languagesSpoken(List.of("english"))
                .travelStyle("adventurous")
                .interests(List.of("surfing", "partying"))
                .build();

        mockMvc.perform(put(ENDPOINT + "/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .header(TRAVEL_ACCEPT_VERSION, TRAVEL_API_V1)
                .content(asJsonString(updatedDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Bob"))
                .andExpect(jsonPath("$.budget_level").value("HIGH"))
                .andExpect(jsonPath("$.interests[0]").value("surfing"));

        List<UserProfile> all = userProfileRepository.findAll();
        assertThat(all).hasSize(1);
        UserProfile userProfile = all.get(0);

        assertThat(userProfile.getName()).isEqualTo("Bob");
        assertThat(userProfile.getBudgetLevel()).isEqualTo(BudgetLevel.HIGH);
        assertThat(userProfile.getGender()).isEqualTo(Gender.MALE);
        assertThat(userProfile.getBirth()).isEqualTo(LocalDate.of(1995, 1, 1));
        assertThat(userProfile.getPreferredClimates()).containsExactlyInAnyOrder("tropical");
        assertThat(userProfile.getLanguagesSpoken()).containsExactlyInAnyOrder("english");
        assertThat(userProfile.getTravelStyle()).isEqualTo("adventurous");
        assertThat(userProfile.getInterests().stream().map(UserInterest::getInterest).toList())
                .containsOnly("surfing", "partying");

    }

    @Test
    void testCreateUserProfile_missingName_shouldReturnBadRequest() throws Exception {
        UserProfileDTO dto = createTestDto();
        dto.setName("");

        mockMvc.perform(post(ENDPOINT)
                .header(TRAVEL_ACCEPT_VERSION, TRAVEL_API_V1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("name: must not be blank")));
    }

    @Test
    void testCreateUserProfile_missingBirth_shouldReturnBadRequest() throws Exception {
        UserProfileDTO dto = createTestDto();
        dto.setBirth(null);

        mockMvc.perform(post(ENDPOINT)
                .header(TRAVEL_ACCEPT_VERSION, TRAVEL_API_V1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("birth: must not be null")));
    }

    @Test
    void testCreateUserProfile_missingBudgetLevel_shouldReturnBadRequest() throws Exception {
        UserProfileDTO dto = createTestDto();
        dto.setBudgetLevel(null);

        mockMvc.perform(post(ENDPOINT)
                .header(TRAVEL_ACCEPT_VERSION, TRAVEL_API_V1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("budgetLevel: must not be null")));
    }

    @Test
    void testCreateUserProfile_missingGender_shouldReturnBadRequest() throws Exception {
        UserProfileDTO dto = createTestDto();
        dto.setGender(null);

        mockMvc.perform(post(ENDPOINT)
                .header(TRAVEL_ACCEPT_VERSION, TRAVEL_API_V1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("gender: must not be null")));
    }

    @Test
    void testCreateUserProfile_missingTravelStyle_shouldReturnBadRequest() throws Exception {
        UserProfileDTO dto = createTestDto();
        dto.setTravelStyle(null);

        mockMvc.perform(post(ENDPOINT)
                .header(TRAVEL_ACCEPT_VERSION, TRAVEL_API_V1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("travelStyle: must not be empty")));
    }

    @Test
    void testCreateUserProfile_missingPreferredClimates_shouldReturnBadRequest() throws Exception {
        UserProfileDTO dto = createTestDto();
        dto.setPreferredClimates(List.of());

        mockMvc.perform(post(ENDPOINT)
                .header(TRAVEL_ACCEPT_VERSION, TRAVEL_API_V1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("preferredClimates: must not be empty")));
    }

    @Test
    void testCreateUserProfile_missingInterests_shouldReturnBadRequest() throws Exception {
        UserProfileDTO dto = createTestDto();
        dto.setInterests(List.of());

        mockMvc.perform(post(ENDPOINT)
                .header(TRAVEL_ACCEPT_VERSION, TRAVEL_API_V1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("interests: must not be empty")));
    }

    @Test
    void testCreateUserProfile_missingLanguagesSpoken_shouldReturnBadRequest() throws Exception {
        UserProfileDTO dto = createTestDto();
        dto.setLanguagesSpoken(List.of());

        mockMvc.perform(post(ENDPOINT)
                .header(TRAVEL_ACCEPT_VERSION, TRAVEL_API_V1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("languagesSpoken: must not be empty")));
    }

    private UserProfileDTO createTestDto() {
        return UserProfileDTO.builder()
                .name("Alice")
                .birth(LocalDate.of(1990, 5, 20))
                .gender(Gender.FEMALE)
                .budgetLevel(BudgetLevel.MEDIUM)
                .preferredClimates(List.of("warm", "tropical"))
                .languagesSpoken(List.of("english", "french"))
                .travelStyle("relaxed")
                .interests(List.of("yoga", "local food", "hiking"))
                .passports(List.of("BR", "ES"))
                .build();
    }

    private String asJsonString(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
