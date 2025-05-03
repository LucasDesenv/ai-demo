package com.ai.demo.travel.controller;

import static com.ai.demo.travel.controller.BudgetController.ENDPOINT;
import static com.ai.demo.travel.controller.TravelApiVersion.TRAVEL_ACCEPT_VERSION;
import static com.ai.demo.travel.controller.TravelApiVersion.TRAVEL_API_V1;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ai.demo.BaseControllerIT;
import com.ai.demo.travel.helper.TravelPlanHelper;
import com.ai.demo.travel.model.BudgetLevel;
import com.ai.demo.travel.model.Gender;
import com.ai.demo.travel.model.UserProfile;
import com.ai.demo.travel.model.repository.TravelPlanRepository;
import com.ai.demo.travel.model.repository.UserProfileRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

class BudgetControllerIT extends BaseControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private TravelPlanRepository travelPlanRepository;

    private Long travelPlanId;

    private final String aiAnswer = "{\n" + "  \"costs\": [\n" + "    {\n" + "      \"description\": \"Flight\",\n"
            + "      \"cost\": 800.00,\n" + "      \"notes\": \"Round trip from Spain to Brazil (Florianopolis)\"\n"
            + "    },\n" + "    {\n" + "      \"description\": \"Accommodation\",\n" + "      \"cost\": 60.00,\n"
            + "      \"notes\": \"Budget guesthouse stay at €60/night\"\n" + "    },\n" + "    {\n"
            + "      \"description\": \"Daily food\",\n" + "      \"cost\": 25.00,\n"
            + "      \"notes\": \"Estimated cost for meals and drinks\"\n" + "    },\n" + "    {\n"
            + "      \"description\": \"Local transportation\",\n" + "      \"cost\": 10.00,\n"
            + "      \"notes\": \"Cost for taxis and buses\"\n" + "    },\n" + "    {\n"
            + "      \"description\": \"Entertainment and activities\",\n" + "      \"cost\": 50.00,\n"
            + "      \"notes\": \"Optional tours and entrance fees\"\n" + "    }\n" + "  ],\n"
            + "  \"total_estimated\": 2425.00\n" + "}";

    @BeforeEach
    void setup() {
        travelPlanRepository.deleteAll();
        userProfileRepository.deleteAll();

        UserProfile user = UserProfile.builder()
                .name("Luca")
                .birth(LocalDate.of(1990, 1, 1))
                .gender(Gender.MALE)
                .budgetLevel(BudgetLevel.HIGH)
                .preferredClimates(List.of("cold"))
                .languagesSpoken(List.of("english"))
                .passports(List.of("DE"))
                .travelStyle("luxury")
                .build();

        Long userId = userProfileRepository.save(user).getId();

        var travelPlan = travelPlanRepository.save(TravelPlanHelper.withSingleDestination(userId));
        this.travelPlanId = travelPlan.getId();
    }

    @Test
    void testEstimateBudget_success() throws Exception {
        when(chatGptService.ask(any())).thenReturn(aiAnswer);
        mockMvc.perform(post(ENDPOINT.replace("{travelPlanId}", travelPlanId.toString()))
                .header(TRAVEL_ACCEPT_VERSION, TRAVEL_API_V1)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.breakdowns").isArray());
    }

    @Test
    void testGetBudget_success() throws Exception {
        when(chatGptService.ask(any())).thenReturn(aiAnswer);
        var created = mockMvc.perform(post(ENDPOINT.replace("{travelPlanId}", travelPlanId.toString()))
                .header(TRAVEL_ACCEPT_VERSION, TRAVEL_API_V1)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        String id = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(get(ENDPOINT.replace("{travelPlanId}", travelPlanId.toString()))
                .header(TRAVEL_ACCEPT_VERSION, TRAVEL_API_V1)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    void testEstimateBudget_invalidTravelPlan_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post(ENDPOINT.replace("{travelPlanId}", "999999"))
                .header(TRAVEL_ACCEPT_VERSION, TRAVEL_API_V1)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Travel plan 999999 not found"));
    }
}
