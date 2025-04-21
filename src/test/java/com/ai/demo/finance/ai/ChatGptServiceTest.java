package com.ai.demo.finance.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.ai.demo.finance.dto.RetirementDetailDTO;
import com.ai.demo.finance.dto.UserDTO;
import com.ai.demo.finance.model.cache.InflationRate;
import com.ai.demo.finance.model.cache.RetirementGoal;
import com.ai.demo.finance.model.enums.Country;
import com.ai.demo.finance.service.InflationService;
import com.ai.demo.finance.service.RetirementService;
import com.ai.demo.finance.service.UserService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class ChatGptServiceTest {

    @Mock
    private RestTemplate restTemplate;
    @Mock
    private UserService userService;
    @Mock
    private InflationService inflationService;
    @Mock
    private RetirementService retirementService;
    private ChatGptService chatGptService;
    private static final String API_KEY = "dummy-api-key";

    @BeforeEach
    public void setUp() {
        chatGptService = new ChatGptService(restTemplate, API_KEY, "gpt-3.5-turbo", retirementService, inflationService, userService);
    }

    @Test
    void shouldReturnPersonalizedAdvice_whenValidInput() {
        Long userId = 1L;
        UserDTO user = new UserDTO(userId, "john_doe", Country.US);

        RetirementDetailDTO retirementDetail = new RetirementDetailDTO(1L, BigDecimal.valueOf(3000), LocalDate.of(2045, 1, 1),
                LocalDate.of(2100, 1, 1),
                "john_doe");

        RetirementGoal retirementGoal = new RetirementGoal(userId, BigDecimal.valueOf(75), BigDecimal.valueOf(200000));

        InflationRate inflationRate = InflationRate.builder()
                .percentageRate(BigDecimal.valueOf(2.5))
                .build();

        ChatGptRequest.Message message = new ChatGptRequest.Message();
        message.setContent("Increase savings rate.");
        ChatGptResponse.Choice choice = new ChatGptResponse.Choice();
        choice.setMessage(message);

        ChatGptResponse chatGptResponse = new ChatGptResponse();
        chatGptResponse.setChoices(List.of(choice));

        when(userService.findById(userId)).thenReturn(user);
        when(retirementService.findByUserId(userId)).thenReturn(Optional.of(retirementDetail));
        when(inflationService.fetchLatestMonthlyInflationRateForYearToDate(Country.US)).thenReturn(Optional.of(inflationRate));
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(ChatGptResponse.class)))
                .thenReturn(ResponseEntity.ok(chatGptResponse));

        String result = chatGptService.personalizedFinancialAdvice(userId, retirementGoal);

        assertNotNull(result);
        assertEquals("Increase savings rate.", result);
    }

    @Test
    void shouldReturnNull_whenNoRetirementPlanExists() {
        Long userId = 2L;
        UserDTO user = new UserDTO(userId, "john_doe", Country.US);

        RetirementGoal goal = new RetirementGoal(userId, BigDecimal.valueOf(60), BigDecimal.valueOf(100000));

        when(userService.findById(userId)).thenReturn(user);
        when(retirementService.findByUserId(userId)).thenReturn(Optional.empty());

        String result = chatGptService.personalizedFinancialAdvice(userId, goal);

        assertNull(result);
        verifyNoInteractions(restTemplate);
    }

}
