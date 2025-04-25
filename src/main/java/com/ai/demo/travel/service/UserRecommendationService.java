package com.ai.demo.travel.service;

import com.ai.demo.travel.dto.UserRecommendationDTO;
import com.ai.demo.travel.mapper.UserRecommendationMapper;
import com.ai.demo.travel.model.UserProfile;
import com.ai.demo.travel.model.UserRecommendation;
import com.ai.demo.travel.model.repository.UserRecommendationRepository;
import java.util.List;
import lombok.AllArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class UserRecommendationService {

    private static final UserRecommendationMapper MAPPER = Mappers.getMapper(UserRecommendationMapper.class);

    private final TravelAssistanceService travelAssistanceService;
    private final UserRecommendationRepository recommendationRepository;

    public UserRecommendationDTO recommend(Long userProfileId) {
        String recommendDestinations = travelAssistanceService.recommendDestinations(userProfileId);
        UserRecommendation recommendation = UserRecommendation.builder()
                .userProfile(UserProfile.builder()
                        .id(userProfileId)
                        .build())
                .content(recommendDestinations)
                .build();

        UserRecommendation saved = recommendationRepository.save(recommendation);

        return MAPPER.toDTO(saved);
    }

    public List<UserRecommendationDTO> findAllByUserId(Long userProfileId) {
        return recommendationRepository.findByUserProfileId(userProfileId)
                .stream()
                .map(MAPPER::toDTO)
                .toList();

    }

}
