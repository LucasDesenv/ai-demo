package com.ai.demo.finance.controller;

import static com.ai.demo.finance.controller.ApiVersion.ACCEPT_VERSION;
import static com.ai.demo.finance.controller.ApiVersion.API_V1;
import static com.ai.demo.finance.model.enums.AccountType.SAVINGS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ai.demo.finance.config.RedisConfigForIntegrationTest;
import com.ai.demo.finance.dto.AccountDTO;
import com.ai.demo.finance.dto.BalanceDTO;
import com.ai.demo.finance.model.Account;
import com.ai.demo.finance.model.RetirementDetail;
import com.ai.demo.finance.model.User;
import com.ai.demo.finance.model.enums.Country;
import com.ai.demo.finance.model.repository.AccountRepository;
import com.ai.demo.finance.model.repository.RetirementRepository;
import com.ai.demo.finance.model.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

@SpringBootTest(classes = {RedisConfigForIntegrationTest.class})
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AccountControllerIT {
    private User defaultUser;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private RetirementRepository retirementRepository;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        defaultUser = userRepository.saveAndFlush(new User(null, "john", Country.ES));
        System.out.println(defaultUser.getId());
    }

    @AfterEach
    void tearDown() {
        accountRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testCreateAccount() throws Exception {
        AccountDTO accountDTO = new AccountDTO(1L, new BigDecimal("1000"), SAVINGS, LocalDateTime.now(),
                defaultUser.getUsername());

        mockMvc.perform(post(AccountController.ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .header(ACCEPT_VERSION, API_V1)
                .content(asJsonString(accountDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void testGetAccount() throws Exception {
        Account saved = accountRepository.save(
                Account.builder().amount(new BigDecimal("1000")).type(SAVINGS).date(LocalDateTime.now())
                        .userId(defaultUser.getId())
                        .build());
        Long id = saved.getId();

        mockMvc.perform(get(AccountController.ENDPOINT + "/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .header(ACCEPT_VERSION, API_V1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.amount").value("1000.0"))
                .andExpect(jsonPath("$.type").value("SAVINGS"))
                .andExpect(jsonPath("$.date").value(saved.getDate().toString()));
    }

    @Test
    void testUpdateAccount() throws Exception {
        Account saved = accountRepository.save(
                Account.builder().amount(new BigDecimal("1000")).type(SAVINGS)
                        .userId(defaultUser.getId())
                        .date(LocalDateTime.now()).build());
        Long id = saved.getId();

        AccountDTO dto = new AccountDTO(id, new BigDecimal("2000"), SAVINGS, LocalDateTime.now(), defaultUser.getUsername());

        mockMvc.perform(put(AccountController.ENDPOINT + "/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .header(ACCEPT_VERSION, API_V1)
                .content(asJsonString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.amount").value("2000"))
                .andExpect(jsonPath("$.type").value("SAVINGS"))
                .andExpect(jsonPath("$.date").value(dto.date().toString()));
    }

    @Test
    void testDeleteAccount() throws Exception {
        Account saved = accountRepository.save(
                Account.builder().amount(new BigDecimal("1000")).type(SAVINGS)
                        .userId(defaultUser.getId())
                        .date(LocalDateTime.now()).build());
        Long id = saved.getId();

        mockMvc.perform(delete(AccountController.ENDPOINT + "/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .header(ACCEPT_VERSION, API_V1))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDepositSuccess() throws Exception {
        Account saved = accountRepository.save(
                Account.builder().amount(new BigDecimal("1000")).type(SAVINGS)
                        .userId(defaultUser.getId())
                        .date(LocalDateTime.now()).build());
        retirementRepository.save(RetirementDetail.builder()
                .userId(defaultUser.getId())
                .incomePerMonthDesired(BigDecimal.valueOf(1200))
                .retirementDate(LocalDate.now().plusYears(30))
                .lifeExpectation(LocalDate.now().plusYears(50))
                .build());
        Long id = saved.getId();
        BalanceDTO balanceDTO = new BalanceDTO(new BigDecimal("500"));
        mockMvc.perform(patch(AccountController.ENDPOINT + "/" + id + "/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .header(ACCEPT_VERSION, API_V1)
                .content(asJsonString(balanceDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value("1500.0"));
    }

    @Test
    void testDepositAccountNotFound() throws Exception {
        Long id = 999L;
        BalanceDTO balanceDTO = new BalanceDTO(new BigDecimal("500"));
        mockMvc.perform(patch(AccountController.ENDPOINT + "/" + id + "/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .header(ACCEPT_VERSION, API_V1)
                .content(asJsonString(balanceDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDepositInvalidAmount() throws Exception {
        Account saved = accountRepository.save(
                Account.builder().amount(new BigDecimal("1000"))
                        .userId(defaultUser.getId())
                        .type(SAVINGS).date(LocalDateTime.now()).build());
        Long id = saved.getId();
        BalanceDTO balanceDTO = new BalanceDTO(new BigDecimal("-500"));
        mockMvc.perform(patch(AccountController.ENDPOINT + "/" + id + "/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .header(ACCEPT_VERSION, API_V1)
                .content(asJsonString(balanceDTO)))
                .andExpect(status().isBadRequest());
    }

    private String asJsonString(final Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}