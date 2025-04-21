package com.ai.demo.travel.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ai.demo.finance.ai.ChatGptService;
import com.ai.demo.travel.dto.TravelAssistanceRequestDTO;
import com.ai.demo.travel.dto.UserProfileDTO;
import com.ai.demo.travel.model.BudgetLevel;
import com.ai.demo.travel.model.Gender;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TravelAssistanceServiceTest {

    @Mock
    private ChatGptService chatGptService;

    @Mock
    private UserProfileService userProfileService;

    @InjectMocks
    private TravelAssistanceService travelAssistanceService;

    @Test
    void test_recommend_returnsExpectedResponse() {
        // Arrange
        Long profileId = 1L;
        UserProfileDTO mockProfile = UserProfileDTO.builder()
                .id(profileId)
                .name("Lena")
                .birth(LocalDate.of(1993, 4, 21))
                .gender(Gender.FEMALE)
                .budgetLevel(BudgetLevel.MEDIUM)
                .preferredClimates(List.of("warm", "tropical"))
                .languagesSpoken(List.of("english", "french"))
                .travelStyle("relaxed")
                .interests(List.of("beach", "yoga", "local food"))
                .build();

        when(userProfileService.findById(profileId)).thenReturn(mockProfile);
        when(chatGptService.ask(anyString())).thenReturn("Bali, Lisbon, and Barcelona would be great for you!");

        // Act
        String result = travelAssistanceService.recommendDestinations(profileId);

        // Assert
        verify(userProfileService).findById(profileId);
        verify(chatGptService).ask(anyString());
        assertEquals("Bali, Lisbon, and Barcelona would be great for you!", result);
    }

    @Test
    void test_requirements_whenUserHasNoPassports_returnsErrorMessage() {
        // Arrange
        Long userId = 1L;
        String destination = "JP";
        TravelAssistanceRequestDTO request = new TravelAssistanceRequestDTO(userId, 10);
        UserProfileDTO user = UserProfileDTO.builder()
                .id(userId)
                .name("Alex")
                .passports(Collections.emptyList())
                .build();

        when(userProfileService.findById(userId)).thenReturn(user);

        // Act
        String result = travelAssistanceService.requirements(destination, request);

        // Assert
        assertEquals("The user does not hold any passport. User needs to provide at least their ID as passport.", result);
    }

    @Test
    void test_requirements_whenUserHasDestinationPassport_returnsMessage() {
        // Arrange
        Long userId = 2L;
        String destination = "BR";
        TravelAssistanceRequestDTO request = new TravelAssistanceRequestDTO(userId, 15);
        UserProfileDTO user = UserProfileDTO.builder()
                .id(userId)
                .name("Maria")
                .passports(List.of("BR", "FR"))
                .build();

        when(userProfileService.findById(userId)).thenReturn(user);

        // Act
        String result = travelAssistanceService.requirements(destination, request);

        // Assert
        assertEquals("The user holds a passport of the destination: BR", result);
    }

    @Test
    void test_requirements_whenUserNeedsVisa_callsChatGptAndReturnsResponse() {
        // Arrange
        Long userId = 3L;
        String destination = "TH";
        TravelAssistanceRequestDTO request = new TravelAssistanceRequestDTO(userId, 20);
        UserProfileDTO user = UserProfileDTO.builder()
                .id(userId)
                .name("Lena")
                .passports(List.of("US", "DE"))
                .build();

        String expectedPrompt = """
                You are a travel advisor helping a solo traveler understand visa and entry requirements.

                The traveler holds the following passports: US, DE
                They are planning to visit: TH
                Intended length of stay: 20 days

                Please answer the following:

                1. Does the traveler need a visa to visit TH with any of their passports?
                2. Which passport offers the most favorable entry (e.g. visa-free or visa on arrival)?
                3. Are there any other entry requirements the traveler should be aware of? Include:
                   - Required or recommended vaccinations
                   - Travel insurance requirements
                   - Proof of onward travel
                   - COVID-19 related rules (if still applicable)
                4. Keep your answer concise and practical. Use bullet points.

                Assume this is for tourism only.
                """;

        when(userProfileService.findById(userId)).thenReturn(user);
        when(chatGptService.ask(expectedPrompt)).thenReturn("US passport allows visa-free entry for 30 days.");

        // Act
        String result = travelAssistanceService.requirements(destination, request);

        // Assert
        assertEquals("US passport allows visa-free entry for 30 days.", result);
        verify(chatGptService).ask(expectedPrompt);
    }
}
