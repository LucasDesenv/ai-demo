package com.ai.demo.finance.controller;

import static com.ai.demo.finance.controller.ApiVersion.ACCEPT_VERSION;
import static com.ai.demo.finance.controller.ApiVersion.API_V1;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ai.demo.BaseControllerIT;
import com.ai.demo.finance.ai.ChatGptService;
import com.ai.demo.finance.dto.RetirementDetailDTO;
import com.ai.demo.finance.model.RetirementDetail;
import com.ai.demo.finance.model.User;
import com.ai.demo.finance.model.enums.Country;
import com.ai.demo.finance.model.repository.RetirementRepository;
import com.ai.demo.finance.model.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

class RetirementControllerIT extends BaseControllerIT {

    private User defaultUser;
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private RetirementRepository retirementRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;
    @MockBean
    private ChatGptService chatGptService;

    @BeforeEach
    void setUp() {
        clearDependencies();
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        defaultUser = userRepository.save(new User(null, "john", Country.BR));
    }

    @AfterEach
    void clearDependencies() {
        retirementRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testCreateRetirementDetail() throws Exception {
        RetirementDetailDTO retirementDetail = new RetirementDetailDTO(1L, new BigDecimal("5000"), LocalDate.now().plusYears(50),
                LocalDate.of(2025, 1, 1), "john");

        mockMvc.perform(post(RetirementController.ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .header(ACCEPT_VERSION, API_V1)
                .content(asJsonString(retirementDetail)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void testGetRetirementDetail() throws Exception {
        RetirementDetail saved = retirementRepository.save(
                RetirementDetail.builder().lifeExpectation(LocalDate.now().plusYears(50)).retirementDate(LocalDate.now())
                        .incomePerMonthDesired(BigDecimal.TEN)
                        .userId(defaultUser.getId())
                        .build());
        Long id = saved.getId();

        mockMvc.perform(get(RetirementController.ENDPOINT + "/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .header(ACCEPT_VERSION, API_V1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.lifeExpectation").value(saved.getLifeExpectation().toString()))
                .andExpect(jsonPath("$.retirementDate").value(saved.getRetirementDate().toString()))
                .andExpect(jsonPath("$.incomePerMonthDesired").value("10.0"));
    }

    @Test
    void testUpdateRetirementDetail() throws Exception {
        RetirementDetail saved = retirementRepository.save(
                RetirementDetail.builder().lifeExpectation(LocalDate.now().plusYears(50)).retirementDate(LocalDate.now())
                        .incomePerMonthDesired(BigDecimal.TEN)
                        .userId(defaultUser.getId())
                        .build());
        Long id = saved.getId();

        RetirementDetailDTO dto = new RetirementDetailDTO(id, new BigDecimal("5000"), LocalDate.now().plusYears(50), LocalDate.of(2025, 1, 1),
                "user");

        mockMvc.perform(put(RetirementController.ENDPOINT + "/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .header(ACCEPT_VERSION, API_V1)
                .content(asJsonString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.lifeExpectation").value(dto.lifeExpectation().toString()))
                .andExpect(jsonPath("$.retirementDate").value(dto.retirementDate().toString()))
                .andExpect(jsonPath("$.incomePerMonthDesired").value(dto.incomePerMonthDesired()));
    }

    @Test
    void testDeleteRetirementDetail() throws Exception {
        RetirementDetail saved = retirementRepository.save(
                RetirementDetail.builder().lifeExpectation(LocalDate.now().plusYears(50)).retirementDate(LocalDate.now())
                        .incomePerMonthDesired(BigDecimal.TEN)
                        .userId(defaultUser.getId())
                        .build());
        Long id = saved.getId();

        mockMvc.perform(delete(RetirementController.ENDPOINT + "/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .header(ACCEPT_VERSION, API_V1))
                .andExpect(status().isNoContent());
    }

    private String asJsonString(final Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}