package com.ai.demo.travel.controller;

import static com.ai.demo.travel.controller.TravelApiVersion.TRAVEL_ACCEPT_VERSION;
import static com.ai.demo.travel.controller.TravelApiVersion.TRAVEL_API_V1;
import static com.ai.demo.travel.controller.TravelPlanController.ENDPOINT;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ai.demo.travel.dto.TravelPlanDTO;
import com.ai.demo.travel.model.BudgetLevel;
import com.ai.demo.travel.model.Gender;
import com.ai.demo.travel.model.TripType;
import com.ai.demo.travel.model.UserProfile;
import com.ai.demo.travel.model.repository.TravelPlanRepository;
import com.ai.demo.travel.model.repository.UserProfileRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

class TravelPlanControllerIT extends BaseControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private TravelPlanRepository travelPlanRepository;

    private Long userId;

    @BeforeEach
    void setup() {
        travelPlanRepository.deleteAll();
        userProfileRepository.deleteAll();

        UserProfile user = UserProfile.builder()
                .name("Lina")
                .birth(LocalDate.of(1992, 4, 15))
                .gender(Gender.FEMALE)
                .budgetLevel(BudgetLevel.MEDIUM)
                .preferredClimates(List.of("warm"))
                .languagesSpoken(List.of("english"))
                .interests(List.of())
                .passports(List.of("US"))
                .travelStyle("adventurous")
                .build();

        userId = userProfileRepository.save(user).getId();
    }

    @Test
    void testCreateAndGetTravelPlan() throws Exception {
        TravelPlanDTO dto = TravelPlanDTO.builder()
                .userProfileId(userId)
                .destinationCountries(Collections.singletonList("IT"))
                .destinationCities(Collections.singletonList("Rome"))
                .startDate(LocalDate.of(2025, 7, 1))
                .endDate(LocalDate.of(2025, 7, 10))
                .tripType(TripType.VACATION)
                .notes("Summer escape")
                .build();

        String body = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post(ENDPOINT.replace("{userId}", String.valueOf(userId)))
                .header(TRAVEL_ACCEPT_VERSION, TRAVEL_API_V1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user_profile_id").value(userId))
                .andExpect(jsonPath("$.destination_cities").value(Matchers.hasSize(1)))
                .andExpect(jsonPath("$.destination_cities[0]").value("Rome"));

        mockMvc.perform(get(ENDPOINT.replace("{userId}", String.valueOf(userId)))
                .header(TRAVEL_ACCEPT_VERSION, TRAVEL_API_V1)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(Matchers.hasSize(1)))
                .andExpect(jsonPath("$[0].destination_countries[0]").value("IT"))
                .andExpect(jsonPath("$[0].destination_countries").value(Matchers.hasSize(1)));
    }

    @Test
    void testCreateTravelPlan_missingDestinationCountry_shouldReturnBadRequest() throws Exception {
        TravelPlanDTO dto = TravelPlanDTO.builder()
                .userProfileId(userId)
                .destinationCountries(Collections.emptyList()) // Missing countries
                .destinationCities(Collections.singletonList("Rome"))
                .startDate(LocalDate.of(2025, 7, 1))
                .endDate(LocalDate.of(2025, 7, 10))
                .tripType(TripType.VACATION)
                .notes("Summer escape")
                .build();

        String body = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post(ENDPOINT.replace("{userId}", String.valueOf(userId)))
                .header(TRAVEL_ACCEPT_VERSION, TRAVEL_API_V1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("destinationCountries: must not be empty")));
    }

    @Test
    void testCreateTravelPlan_endDateBeforeStartDate_shouldReturnBadRequest() throws Exception {
        TravelPlanDTO dto = TravelPlanDTO.builder()
                .userProfileId(userId)
                .destinationCountries(Collections.singletonList("FR"))
                .destinationCities(Collections.singletonList("Paris"))
                .startDate(LocalDate.of(2025, 7, 10))
                .endDate(LocalDate.of(2025, 7, 1)) // End before start
                .tripType(TripType.VACATION)
                .notes("Backwards trip")
                .build();

        String body = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post(ENDPOINT.replace("{userId}", String.valueOf(userId)))
                .header(TRAVEL_ACCEPT_VERSION, TRAVEL_API_V1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Start date cannot be after end date")));
    }

    @Test
    void testCreateTravelPlan_invalidUser_shouldReturnBadRequest() throws Exception {
        Long invalidUserId = 999999L; // Assuming this user does not exist

        TravelPlanDTO dto = TravelPlanDTO.builder()
                .userProfileId(invalidUserId)
                .destinationCountries(Collections.singletonList("JP"))
                .destinationCities(Collections.singletonList("Tokyo"))
                .startDate(LocalDate.of(2025, 5, 1))
                .endDate(LocalDate.of(2025, 5, 15))
                .tripType(TripType.VACATION)
                .notes("Explore Japan")
                .build();

        String body = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post(ENDPOINT.replace("{userId}", String.valueOf(invalidUserId)))
                .header(TRAVEL_ACCEPT_VERSION, TRAVEL_API_V1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest());
    }

}
