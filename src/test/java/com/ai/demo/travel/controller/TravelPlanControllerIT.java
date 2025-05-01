package com.ai.demo.travel.controller;

import static com.ai.demo.travel.controller.TravelApiVersion.TRAVEL_ACCEPT_VERSION;
import static com.ai.demo.travel.controller.TravelApiVersion.TRAVEL_API_V1;
import static com.ai.demo.travel.controller.TravelPlanController.ENDPOINT;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ai.demo.BaseControllerIT;
import com.ai.demo.travel.dto.TravelPlanDTO;
import com.ai.demo.travel.dto.TravelPlanDestinationDTO;
import com.ai.demo.travel.model.BudgetLevel;
import com.ai.demo.travel.model.Gender;
import com.ai.demo.travel.model.TripType;
import com.ai.demo.travel.model.UserProfile;
import com.ai.demo.travel.model.repository.TravelPlanRepository;
import com.ai.demo.travel.model.repository.UserProfileRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.ArrayList;
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
                .destinations(Collections.singletonList(
                        TravelPlanDestinationDTO.builder().stayingDays(15L).city("Rome").country("IT").build()))
                .startDate(LocalDate.of(2025, 7, 1))
                .endDate(LocalDate.of(2025, 7, 10))
                .tripType(TripType.VACATION)
                .originCountry("BR")
                .notes("Summer escape")
                .build();

        String body = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post(ENDPOINT.replace("{userId}", String.valueOf(userId)))
                .header(TRAVEL_ACCEPT_VERSION, TRAVEL_API_V1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user_profile_id").value(userId))
                .andExpect(jsonPath("$.destinations").value(Matchers.hasSize(1)))
                .andExpect(jsonPath("$.destinations[0].staying_days").value(15L))
                .andExpect(jsonPath("$.destinations[0].city").value("Rome"))
                .andExpect(jsonPath("$.destinations[0].country").value("IT"));

        mockMvc.perform(get(ENDPOINT.replace("{userId}", String.valueOf(userId)))
                .header(TRAVEL_ACCEPT_VERSION, TRAVEL_API_V1)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(Matchers.hasSize(1)))
                .andExpect(jsonPath("$[0].destinations[0].staying_days").value(15L))
                .andExpect(jsonPath("$[0].destinations[0].country").value("IT"))
                .andExpect(jsonPath("$[0].destinations[0].created_at").exists())
                .andExpect(jsonPath("$[0].destinations[0].last_modified_at").exists())
                .andExpect(jsonPath("$[0].destinations").value(Matchers.hasSize(1)))
                .andExpect(jsonPath("$[0].created_at").exists())
                .andExpect(jsonPath("$[0].last_modified_at").exists());
    }

    @Test
    void testCreateTravelPlan_missingRequiredFields_shouldReturnBadRequest() throws Exception {
        TravelPlanDTO dto = TravelPlanDTO.builder()
                .userProfileId(userId)
                .destinations(Collections.emptyList())
                .startDate(LocalDate.of(2025, 7, 1))
                .endDate(LocalDate.of(2025, 7, 10))
                .notes("Summer escape")
                .build();

        String body = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post(ENDPOINT.replace("{userId}", String.valueOf(userId)))
                .header(TRAVEL_ACCEPT_VERSION, TRAVEL_API_V1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("destinations: must not be empty")))
                .andExpect(jsonPath("$.message").value(containsString("originCountry: must not be blank")))
                .andExpect(jsonPath("$.message").value(containsString("originCountry: Invalid country code")))
                .andExpect(jsonPath("$.message").value(containsString("tripType: must not be null")));
    }

    @Test
    void testCreateTravelPlan_maxDestinations_shouldReturnBadRequest() throws Exception {
        TravelPlanDTO dto = TravelPlanDTO.builder().userProfileId(userId)
                .destinations(new ArrayList<>())
                .startDate(LocalDate.of(2025, 7, 1))
                .endDate(LocalDate.of(2025, 7, 10))
                .tripType(TripType.VACATION).originCountry("BR").notes("Summer escape").build();

        for (int i = 0; i < 6; i++) {
            dto.getDestinations().add(
                    TravelPlanDestinationDTO.builder().stayingDays(15L).city("Rome_" + i).country("IT")
                            .build());
        }

        String body = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post(ENDPOINT.replace("{userId}", String.valueOf(userId)))
                .header(TRAVEL_ACCEPT_VERSION, TRAVEL_API_V1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("destinations: size must be between 1 and 5")));
    }

    @Test
    void testCreateTravelPlan_endDateBeforeStartDate_shouldReturnBadRequest() throws Exception {
        TravelPlanDTO dto = TravelPlanDTO.builder()
                .userProfileId(userId)
                .destinations(Collections.singletonList(
                        TravelPlanDestinationDTO.builder().stayingDays(15L).city("Paris").country("FR").build()))
                .startDate(LocalDate.of(2025, 7, 10))
                .endDate(LocalDate.of(2025, 7, 1)) // End before start
                .tripType(TripType.VACATION)
                .originCountry("BR")
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
                .destinations(Collections.singletonList(
                        TravelPlanDestinationDTO.builder().stayingDays(15L).city("Paris").country("FR").build()))
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
