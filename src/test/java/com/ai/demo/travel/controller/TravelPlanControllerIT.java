package com.ai.demo.travel.controller;

import static com.ai.demo.travel.controller.TravelApiVersion.TRAVEL_ACCEPT_VERSION;
import static com.ai.demo.travel.controller.TravelApiVersion.TRAVEL_API_V1;
import static com.ai.demo.travel.controller.TravelPlanController.ENDPOINT;
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
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TravelPlanControllerIT {

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
}
