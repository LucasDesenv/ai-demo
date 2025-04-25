package com.ai.demo.travel.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ai.demo.travel.dto.UserRecommendationDTO;
import com.ai.demo.travel.model.UserProfile;
import com.ai.demo.travel.model.UserRecommendation;
import com.ai.demo.travel.model.repository.UserRecommendationRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserRecommendationServiceTest {

    @Mock
    private TravelAssistanceService travelAssistanceService;

    @Mock
    private UserRecommendationRepository recommendationRepository;

    @InjectMocks
    private UserRecommendationService recommendationService;

    @Test
    void test_recommend_shouldCallTravelServiceAndSaveRecommendation() {
        // Arrange
        Long userId = 1L;
        String gptResponse = "Bali, Lisbon, and Barcelona would be great for you!";

        when(travelAssistanceService.recommendDestinations(userId)).thenReturn(gptResponse);
        when(recommendationRepository.save(any(UserRecommendation.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        UserRecommendationDTO result = recommendationService.recommend(userId);

        // Assert
        assertEquals(gptResponse, result.content());

        verify(travelAssistanceService).recommendDestinations(userId);
        verify(recommendationRepository).save(any(UserRecommendation.class));
    }

    @Test
    void test_findAllByUserId_shouldReturnMappedDTOs() {
        // Arrange
        Long userId = 2L;
        UserRecommendation rec = UserRecommendation.builder()
                .id(1L)
                .userProfile(UserProfile.builder().id(userId).build())
                .content("Lisbon, Portugal is recommended for culture lovers.")
                .createdAt(LocalDateTime.now())
                .build();

        when(recommendationRepository.findByUserProfileId(userId)).thenReturn(List.of(rec));

        // Act
        List<UserRecommendationDTO> results = recommendationService.findAllByUserId(userId);

        // Assert
        assertEquals(1, results.size());
        assertEquals(rec.getContent(), results.get(0).content());
    }
}
