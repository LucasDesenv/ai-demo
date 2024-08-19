package com.ai.demo.finance.controller;

import static com.ai.demo.finance.controller.ApiVersion.ACCEPT_VERSION;
import static com.ai.demo.finance.controller.ApiVersion.API_V1;

import com.ai.demo.finance.model.cache.RetirementGoal;
import com.ai.demo.finance.service.RetirementGoalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(headers = {ACCEPT_VERSION + "=" + API_V1})
@AllArgsConstructor
@Tag(name = "RetirementGoal", description = "APIs related to retirement goal")
public class RetirementGoalController {
    public static final String ENDPOINT = "/user/:id/retirement/goal";
    private final RetirementGoalService retirementGoalService;

    @GetMapping(value = ENDPOINT + "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "Get user's retirement goal by ID")
    public ResponseEntity<RetirementGoal> getGoal(@PathVariable Long id) {
        return ResponseEntity.ok(retirementGoalService.getRetirementGoal(id));
    }

}
