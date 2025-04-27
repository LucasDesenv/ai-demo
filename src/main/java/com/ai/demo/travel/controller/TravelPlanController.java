package com.ai.demo.travel.controller;

import com.ai.demo.travel.dto.TravelPlanDTO;
import com.ai.demo.travel.service.TravelPlanService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(headers = {TravelApiVersion.TRAVEL_ACCEPT_VERSION + "=" + TravelApiVersion.TRAVEL_API_V1})
@RequiredArgsConstructor
@Tag(name = "Travel Plan", description = "APIs related to Travel Plan")
public class TravelPlanController {

    public static final String ENDPOINT = "/user/{userId}/travel-plans";

    private final TravelPlanService service;

    @PostMapping(ENDPOINT)
    public ResponseEntity<TravelPlanDTO> create(@PathVariable Long userId, @Valid @RequestBody TravelPlanDTO dto) {
        return ResponseEntity.ok(service.save(userId, dto));
    }

    @GetMapping(ENDPOINT)
    public ResponseEntity<List<TravelPlanDTO>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(service.findAllByUser(userId));
    }
}
