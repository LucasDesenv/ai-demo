package com.ai.demo.travel.mapper;

import com.ai.demo.travel.dto.UserRecommendationDTO;
import com.ai.demo.travel.model.UserRecommendation;
import org.mapstruct.Mapper;

@Mapper
public interface UserRecommendationMapper {

    UserRecommendationDTO toDTO(UserRecommendation userRecommendation);
}
