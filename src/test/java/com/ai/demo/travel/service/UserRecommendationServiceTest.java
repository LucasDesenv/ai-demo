package com.ai.demo.travel.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ai.demo.travel.dto.UserProfileDTO;
import com.ai.demo.travel.dto.UserRecommendationDTO;
import com.ai.demo.travel.model.UserProfile;
import com.ai.demo.travel.model.UserRecommendation;
import com.ai.demo.travel.model.repository.UserRecommendationRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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

    @Mock
    private UserProfileService userProfileService;

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

    @Test
    void test_recommend_shouldReuseExistingRecommendationIfProfileUnchanged() {
        // Arrange
        Long userId = 5L;
        LocalDateTime now = LocalDateTime.now();

        UserRecommendation existing = UserRecommendation.builder()
                .id(10L)
                .userProfile(UserProfile.builder().id(userId).build())
                .content("Existing recommendation")
                .createdAt(now)
                .build();

        UserProfileDTO profile = UserProfileDTO.builder()
                .id(userId)
                .lastModifiedAt(now.minusMinutes(1)) // Profile last modified BEFORE recommendation
                .build();

        when(recommendationRepository.findTopByUserProfileIdOrderByCreatedAtDesc(userId)).thenReturn(Optional.of(existing));
        when(userProfileService.findById(userId)).thenReturn(profile);

        // Act
        UserRecommendationDTO result = recommendationService.recommend(userId);

        // Assert
        assertEquals("Existing recommendation", result.content());
        verify(travelAssistanceService, org.mockito.Mockito.never()).recommendDestinations(userId);
    }

    @Test
    void test_recommend_shouldGenerateNewRecommendationIfProfileChanged() {
        // Arrange
        Long userId = 6L;
        String newContent = "Fresh GPT recommendation";
        LocalDateTime past = LocalDateTime.now().minusDays(1);
        LocalDateTime now = LocalDateTime.now();

        UserRecommendation previous = UserRecommendation.builder()
                .id(11L)
                .userProfile(UserProfile.builder().id(userId).build())
                .content("Old content")
                .createdAt(past)
                .build();

        UserProfileDTO profile = UserProfileDTO.builder()
                .id(userId)
                .lastModifiedAt(now) // Profile modified AFTER last recommendation
                .build();

        when(recommendationRepository.findTopByUserProfileIdOrderByCreatedAtDesc(userId)).thenReturn(Optional.of(previous));
        when(userProfileService.findById(userId)).thenReturn(profile);
        when(travelAssistanceService.recommendDestinations(userId)).thenReturn(newContent);
        when(recommendationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // Act
        UserRecommendationDTO result = recommendationService.recommend(userId);

        // Assert
        assertEquals(newContent, result.content());
        verify(travelAssistanceService).recommendDestinations(userId);
        verify(recommendationRepository).save(any());
    }

}
