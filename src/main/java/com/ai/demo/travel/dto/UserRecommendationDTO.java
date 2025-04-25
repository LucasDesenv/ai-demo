package com.ai.demo.travel.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public record UserRecommendationDTO(Long id, String content,
        @JsonProperty("created_at") LocalDateTime createdAt) {

}