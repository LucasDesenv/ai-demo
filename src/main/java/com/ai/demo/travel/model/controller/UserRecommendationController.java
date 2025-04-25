package com.ai.demo.travel.model.controller;

import static com.ai.demo.travel.model.controller.TravelApiVersion.TRAVEL_ACCEPT_VERSION;
import static com.ai.demo.travel.model.controller.TravelApiVersion.TRAVEL_API_V1;

import com.ai.demo.travel.dto.UserRecommendationDTO;
import com.ai.demo.travel.service.UserRecommendationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(headers = {TRAVEL_ACCEPT_VERSION + "=" + TRAVEL_API_V1})
@Tag(name = "User Recommendation", description = "APIs related to User Recommendations")
public class UserRecommendationController {

    public static final String ENDPOINT = "/user-recommendation";
    private final UserRecommendationService userRecommendationService;

    public UserRecommendationController(UserRecommendationService userRecommendationService) {
        this.userRecommendationService = userRecommendationService;
    }

    @PostMapping(value = ENDPOINT + "/{userId}/destinations")
    public ResponseEntity<UserRecommendationDTO> recommend(@PathVariable(name = "userId") Long userId) {
        UserRecommendationDTO created = userRecommendationService.recommend(userId);
        return ResponseEntity.created(URI.create(ENDPOINT.concat("/").concat(String.valueOf(created.id()))))
                .body(created);
    }

    @GetMapping(value = ENDPOINT + "/{userId}")
    public List<UserRecommendationDTO> getRecommendation(@PathVariable(name = "userId") Long userId) {
        return userRecommendationService.findAllByUserId(userId);
    }

}
