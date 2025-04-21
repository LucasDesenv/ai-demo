package com.ai.demo.travel.model.controller;

import static com.ai.demo.travel.model.controller.TravelApiVersion.TRAVEL_ACCEPT_VERSION;
import static com.ai.demo.travel.model.controller.TravelApiVersion.TRAVEL_API_V1;

import com.ai.demo.travel.service.RecommendationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(headers = {TRAVEL_ACCEPT_VERSION + "=" + TRAVEL_API_V1})
@Tag(name = "Travel Recommendation", description = "APIs related to Travel Recommendations")
public class RecommendationController {

    public static final String ENDPOINT = "/travel/recommendations";
    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping(value = ENDPOINT)
    public ResponseEntity<String> getRecommendations(@RequestParam(name = "userId") Long userId) {
        return ResponseEntity.ok(recommendationService.recommend(userId));
    }
}
