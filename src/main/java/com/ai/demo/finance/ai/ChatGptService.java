package com.ai.demo.finance.ai;

import com.ai.demo.finance.dto.RetirementDetailDTO;
import com.ai.demo.finance.dto.UserDTO;
import com.ai.demo.finance.model.cache.InflationRate;
import com.ai.demo.finance.model.cache.RetirementGoal;
import com.ai.demo.finance.service.InflationService;
import com.ai.demo.finance.service.RetirementService;
import com.ai.demo.finance.service.UserService;
import java.util.List;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Log4j2
public class ChatGptService {
    private final RestTemplate restTemplate;
    private final String apiKey;
    private final RetirementService retirementService;
    private final InflationService inflationService;
    private final UserService userService;

    public ChatGptService(RestTemplate restTemplate,
            @Value("${openai.api.key}") String apiKey, RetirementService retirementService, InflationService inflationService,
            UserService userService) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.retirementService = retirementService;
        this.inflationService = inflationService;
        this.userService = userService;
    }

    /**
     * Generates personalized financial advice for a user to help reach FIRE sooner,
     * based on their retirement details, current net worth, and the latest
     * inflation rate.
     * @param userId the ID of the user
     * @param retirementGoal the user's retirement goal information
     * @return a string containing tailored financial advice, or null if no
     *         retirement plan exists
     */
    public String personalizedFinancialAdvice(Long userId, RetirementGoal retirementGoal) {
        UserDTO user = userService.findById(userId);
        Optional<RetirementDetailDTO> retirementDetailOpt = retirementService.findByUserId(userId);

        if (retirementDetailOpt.isEmpty()) {
            log.warn("User {} does not have retirement plan to advice.", userId);
            return null;
        }

        RetirementDetailDTO retirementDetail = retirementDetailOpt.get();
        InflationRate inflationRate = inflationService.fetchLatestMonthlyInflationRateForYearToDate(user.country())
                .orElse(InflationRate.noInflation());

        String advicePrompt = "Given this user profile: retirement date = %s, life expectancy = %s, current net worth = €%s, monthly income goal = €%s. What adjustments could the user make to reach FIRE sooner, considering %s percent annual inflation?"
                .formatted(
                        retirementDetail.retirementDate(), retirementDetail.lifeExpectation(), retirementGoal.getCurrentNetWorth(),
                        retirementDetail.incomePerMonthDesired(),
                        inflationRate.getPercentageRate());
        return ask(advicePrompt);
    }

    private String ask(String prompt) {
        ChatGptRequest request = new ChatGptRequest(
                "gpt-3.5-turbo",
                List.of(new ChatGptRequest.Message("user", prompt)));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<ChatGptRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<ChatGptResponse> response = restTemplate.postForEntity(
                "https://api.openai.com/v1/chat/completions",
                entity,
                ChatGptResponse.class);

        return response.getBody()
                .getChoices()
                .get(0)
                .getMessage()
                .getContent();
    }
}