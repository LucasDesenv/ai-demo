package com.ai.demo.finance.controller;

import static com.ai.demo.finance.controller.ApiVersion.ACCEPT_VERSION;
import static com.ai.demo.finance.controller.ApiVersion.API_V1;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ai.demo.finance.dto.UserDTO;
import com.ai.demo.finance.model.User;
import com.ai.demo.finance.model.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void testCreateUser() throws Exception {
        UserDTO userDTO = new UserDTO(null, "testuser");

        mockMvc.perform(post(UserController.ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .header(ACCEPT_VERSION, API_V1)
                .content(asJsonString(userDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void testCreateUserWithExistingUsername() throws Exception {
        userRepository.save(User.builder().username("testuser").build());
        UserDTO userDTO = new UserDTO(null, "testuser");

        mockMvc.perform(post(UserController.ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .header(ACCEPT_VERSION, API_V1)
                .content(asJsonString(userDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateUserWithEmptyUsername() throws Exception {
        UserDTO userDTO = new UserDTO(null, "");

        mockMvc.perform(post(UserController.ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .header(ACCEPT_VERSION, API_V1)
                .content(asJsonString(userDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("username: must not be empty")));

    }

    @Test
    void testCreateUserWithNullUsername() throws Exception {
        UserDTO userDTO = new UserDTO(null, null);

        mockMvc.perform(post(UserController.ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .header(ACCEPT_VERSION, API_V1)
                .content(asJsonString(userDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("username: must not be empty")));
    }

    @Test
    void testCreateUserWithUsernameExceedingMaxLength() throws Exception {
        String longUsername = "a".repeat(16); // Assuming the max length is 15
        UserDTO userDTO = new UserDTO(null, longUsername);

        mockMvc.perform(post(UserController.ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .header(ACCEPT_VERSION, API_V1)
                .content(asJsonString(userDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("username: size must be between 3 and 15")));

    }

    @Test
    void testGetUserByUserName() throws Exception {
        User createdUser = userRepository.save(User.builder().username("lucas").build());

        mockMvc.perform(get(UserController.ENDPOINT + "/" + createdUser.getUsername())
                .contentType(MediaType.APPLICATION_JSON)
                .header(ACCEPT_VERSION, API_V1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdUser.getId()))
                .andExpect(jsonPath("$.username").value(createdUser.getUsername()));
    }

    private String asJsonString(final Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}