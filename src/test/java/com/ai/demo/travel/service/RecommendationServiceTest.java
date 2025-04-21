package com.ai.demo.travel.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ai.demo.finance.ai.ChatGptService;
import com.ai.demo.travel.dto.UserProfileDTO;
import com.ai.demo.travel.model.BudgetLevel;
import com.ai.demo.travel.model.Gender;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private ChatGptService chatGptService;

    @Mock
    private UserProfileService userProfileService;

    @InjectMocks
    private RecommendationService recommendationService;

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
        String result = recommendationService.recommend(profileId);

        // Assert
        verify(userProfileService).findById(profileId);
        verify(chatGptService).ask(anyString());
        assertEquals("Bali, Lisbon, and Barcelona would be great for you!", result);
    }
}
