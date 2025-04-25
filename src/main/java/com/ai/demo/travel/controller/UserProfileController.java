package com.ai.demo.travel.controller;

import com.ai.demo.travel.dto.UserProfileDTO;
import com.ai.demo.travel.service.UserProfileService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(headers = {TravelApiVersion.TRAVEL_ACCEPT_VERSION + "=" + TravelApiVersion.TRAVEL_API_V1})
@Tag(name = "UserProfile", description = "APIs related to User Profiles")
public class UserProfileController {

    public static final String ENDPOINT = "/user-profiles";
    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @PostMapping(value = ENDPOINT)
    public ResponseEntity<UserProfileDTO> createUser(@Valid @RequestBody UserProfileDTO dto) {
        UserProfileDTO createdUser = userProfileService.createUserProfile(dto);
        return ResponseEntity.created(URI.create(ENDPOINT.concat("/").concat(String.valueOf(createdUser.getId()))))
                .body(createdUser);
    }

    @GetMapping(value = ENDPOINT + "/{id}")
    public ResponseEntity<UserProfileDTO> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userProfileService.findById(id));
    }

    @PutMapping(value = ENDPOINT + "/{id}")
    public ResponseEntity<UserProfileDTO> updateUser(@PathVariable Long id, @Valid @RequestBody UserProfileDTO dto) {
        return ResponseEntity.ok(userProfileService.updateUserProfile(id, dto));
    }
}
