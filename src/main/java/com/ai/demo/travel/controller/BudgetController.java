package com.ai.demo.travel.controller;

import com.ai.demo.travel.dto.BudgetEstimationDTO;
import com.ai.demo.travel.service.BudgetEstimationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(headers = {TravelApiVersion.TRAVEL_ACCEPT_VERSION + "=" + TravelApiVersion.TRAVEL_API_V1})
@RequiredArgsConstructor
@Tag(name = "Budget", description = "APIs related to Budget")
public class BudgetController {
    public static final String ENDPOINT = "/travel-plans/{travelPlanId}/budget";

    private final BudgetEstimationService budgetEstimationService;

    @PostMapping(value = ENDPOINT)
    public ResponseEntity<BudgetEstimationDTO> estimateBudget(@PathVariable Long travelPlanId) {
        BudgetEstimationDTO created = budgetEstimationService.estimateBudget(travelPlanId);
        return ResponseEntity.created(URI.create(ENDPOINT.replace("{travelPlanId}", String.valueOf(created.getId())))).body(created);
    }

    @GetMapping(value = ENDPOINT)
    public ResponseEntity<BudgetEstimationDTO> getBudget(@PathVariable Long travelPlanId) {
        return ResponseEntity.ok(budgetEstimationService.findByTravelPlanId(travelPlanId));
    }
}
