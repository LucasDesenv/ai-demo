package com.ai.demo.travel.controller;

import static com.ai.demo.travel.controller.TravelApiVersion.TRAVEL_ACCEPT_VERSION;
import static com.ai.demo.travel.controller.TravelApiVersion.TRAVEL_API_V1;
import static com.ai.demo.travel.controller.UserRecommendationController.ENDPOINT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ai.demo.finance.ai.ChatGptService;
import com.ai.demo.travel.dto.UserProfileDTO;
import com.ai.demo.travel.model.BudgetLevel;
import com.ai.demo.travel.model.Gender;
import com.ai.demo.travel.model.UserRecommendation;
import com.ai.demo.travel.model.repository.UserProfileRepository;
import com.ai.demo.travel.model.repository.UserRecommendationRepository;
import com.ai.demo.travel.service.UserProfileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserRecommendationControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private UserRecommendationRepository recommendationRepository;

    @MockBean
    private ChatGptService chatGptService;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        cleanup();
    }

    @AfterEach
    void cleanup() {
        recommendationRepository.deleteAll();
        userProfileRepository.deleteAll();
    }

    @Test
    void testRecommendDestinationAndRetrieveIt() throws Exception {
        // Create user profile
        UserProfileDTO dto = createUserProfile();

        Long userId = dto.getId();

        Mockito.when(chatGptService.ask(ArgumentMatchers.anyString())).thenReturn("Mocked chatgpt");

        // Request recommendation
        mockMvc.perform(post(ENDPOINT + "/" + userId + "/destinations")
                .contentType(MediaType.APPLICATION_JSON)
                .header(TRAVEL_ACCEPT_VERSION, TRAVEL_API_V1))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.content").value("Mocked chatgpt"))
                .andExpect(jsonPath("$.created_at").exists());

        // Retrieve saved recommendation(s)
        mockMvc.perform(get(ENDPOINT + "/" + userId)
                .contentType(MediaType.APPLICATION_JSON)
                .header(TRAVEL_ACCEPT_VERSION, TRAVEL_API_V1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").isNumber());

        List<UserRecommendation> all = recommendationRepository.findByUserProfileId(userId);
        assertThat(all).hasSize(1);
        assertThat(all.get(0).getContent()).isNotBlank();
    }

    private UserProfileDTO createUserProfile() {
        UserProfileDTO dto = UserProfileDTO.builder().name("Alice").birth(LocalDate.of(1990, 5, 20)).gender(Gender.FEMALE)
                .budgetLevel(BudgetLevel.MEDIUM).preferredClimates(List.of("warm", "tropical"))
                .languagesSpoken(List.of("english", "french")).travelStyle("relaxed")
                .interests(List.of("yoga", "local food", "hiking")).passports(List.of("BR", "ES"))
                .build();
        return userProfileService.createUserProfile(dto);
    }

}
