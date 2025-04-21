package com.ai.demo.travel.model.controller;

import static com.ai.demo.travel.model.controller.TravelApiVersion.TRAVEL_ACCEPT_VERSION;
import static com.ai.demo.travel.model.controller.TravelApiVersion.TRAVEL_API_V1;

import com.ai.demo.travel.dto.TravelAssistanceRequestDTO;
import com.ai.demo.travel.service.TravelAssistanceService;
import com.ai.demo.travel.validator.ValidCountryCode;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(headers = {TRAVEL_ACCEPT_VERSION + "=" + TRAVEL_API_V1})
@Tag(name = "Travel Recommendation", description = "APIs related to Travel Recommendations")
public class TravelAssistanceController {

    public static final String ENDPOINT = "/travel";
    private final TravelAssistanceService travelAssistanceService;

    public TravelAssistanceController(TravelAssistanceService travelAssistanceService) {
        this.travelAssistanceService = travelAssistanceService;
    }

    @GetMapping(value = ENDPOINT + "/destination/recommendations")
    public ResponseEntity<String> getRecommendations(@RequestParam(name = "userId") Long userId) {
        return ResponseEntity.ok(travelAssistanceService.recommendDestinations(userId));
    }

    @PostMapping(value = ENDPOINT + "/{countryDestination}/requirements")
    public ResponseEntity<String> getRequirements(@PathVariable @ValidCountryCode String countryDestination,
            @Valid @RequestBody TravelAssistanceRequestDTO travelAssistanceRequestDTO) {
        return ResponseEntity.ok(travelAssistanceService.requirements(countryDestination, travelAssistanceRequestDTO));
    }
}
