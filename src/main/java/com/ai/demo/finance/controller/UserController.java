package com.ai.demo.finance.controller;

import static com.ai.demo.finance.controller.ApiVersion.ACCEPT_VERSION;
import static com.ai.demo.finance.controller.ApiVersion.API_V1;

import com.ai.demo.finance.dto.UserDTO;
import com.ai.demo.finance.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(headers = {ACCEPT_VERSION + "=" + API_V1})
@AllArgsConstructor
@Tag(name = "User", description = "APIs related to user details")
public class UserController {

    public static final String ENDPOINT = "/user";

    private final UserService userService;

    @PostMapping(value = ENDPOINT, produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "Create a new user")
    public ResponseEntity<UserDTO> createUser(@RequestBody @Valid UserDTO userDTO) {
        UserDTO createdUser = userService.createUser(userDTO);
        return ResponseEntity.created(URI.create(ENDPOINT.concat("/").concat(String.valueOf(createdUser.id()))))
                .body(createdUser);
    }

    @GetMapping(value = ENDPOINT + "/{username}", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "Get user by ID")
    public ResponseEntity<UserDTO> getUserByUserName(@PathVariable String username) {
        UserDTO userDTO = userService.findByUsername(username);
        return ResponseEntity.ok(userDTO);
    }
}