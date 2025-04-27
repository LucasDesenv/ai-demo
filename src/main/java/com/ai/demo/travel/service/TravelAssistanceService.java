package com.ai.demo.travel.service;

import com.ai.demo.finance.ai.ChatGptService;
import com.ai.demo.travel.dto.TravelAssistanceRequestDTO;
import com.ai.demo.travel.dto.UserProfileDTO;
import com.ai.demo.utils.CountryCodesUtil;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * GPT-3.5-turbo uses the cl100k_base tokenizer. On average: 1 token ≈ 3/4 word,
 * so 100 tokens ≈ 75 words. Formula: total chars / 4 💰 OpenAI
 * GPT-3.5-turbo-0125 Pricing (as of April 2025) Input tokens: $0.0005 per 1,000
 * tokens Output tokens: $0.0015 per 1,000 tokens​
 */
@Service
@AllArgsConstructor
public class TravelAssistanceService {
    private final ChatGptService chatGptService;
    private final UserProfileService userProfileService;

    public String recommendDestinations(Long userProfileId) {
        UserProfileDTO userProfile = userProfileService.findById(userProfileId);
        return chatGptService.ask(buildPromptForRecommendations(userProfile));
    }

    /**
     * Total input tokens: 950 length / 4 = 240 tokens Input cost: (240 tokens /
     * 1,000) × $0.0005 = $0.00012 Estimated output tokens: ~400 Output cost: (400
     * tokens / 1,000) × $0.0015 = $0.0006 Estimated Total Cost: $0.00072
     * @param profile
     * @return
     */
    private String buildPromptForRecommendations(UserProfileDTO profile) {
        int age = Period.between(profile.getBirth(), LocalDate.now()).getYears();
        return String.format(
                """
                            You are a travel advisor who specializes in recommending travel destinations for solo travelers. Based on the following user profile, recommend 3 travel destinations that best match the person's interests, preferences, and travel style.

                            For each destination, include:
                            - City and country
                            - 2-3 tags that match the user’s profile (e.g. yoga, food, warm weather)
                            - A short explanation of why this destination is a good fit

                            Name: %s
                            Age: %d
                            Gender: %s
                            Budget Level: %s
                            Preferred Climates: %s
                            Languages Spoken: %s
                            Interests: %s
                            Travel Style: %s

                            Make recommendations that align with the user’s interests, budget, and climate preferences. Prioritize places known to be welcoming for solo travelers.
                        """,
                profile.getName(), age,
                profile.getGender(),
                profile.getBudgetLevel(),
                String.join(", ", profile.getPreferredClimates()),
                String.join(", ", profile.getLanguagesSpoken()),
                String.join(", ", profile.getInterests()),
                profile.getTravelStyle());
    }

    public String requirements(String countryCodeDestination, TravelAssistanceRequestDTO travelAssistanceRequestDTO) {
        UserProfileDTO userProfile = userProfileService.findById(travelAssistanceRequestDTO.getUserId());

        List<String> passports = userProfile.getPassports();

        if (CollectionUtils.isEmpty(passports)) {
            return "The user does not hold any passport. User needs to provide at least their ID as passport.";
        }

        if (passports.contains(countryCodeDestination)) {
            return String.format("The user holds a passport of the destination: %s", countryCodeDestination);
        }

        String prompt = buildPromptForRequirements(userProfile, countryCodeDestination, travelAssistanceRequestDTO);

        return chatGptService.ask(prompt);
    }

    /**
     * Total input tokens: 1150 length / 4 = 290 tokens Input cost: (290 tokens /
     * 1,000) × $0.0005 = $0.000145 Total output tokens: ~450 Output cost: (450
     * tokens / 1,000) × $0.0015 = $0.000675 Estimated Total cost: $0.000145 +
     * $0.000675 = $0.00082
     * @param userProfile
     * @param countryCodeDestination
     * @param travelAssistanceRequestDTO
     * @return
     */
    private String buildPromptForRequirements(UserProfileDTO userProfile,
            String countryCodeDestination, TravelAssistanceRequestDTO travelAssistanceRequestDTO) {

        Integer numberOfStayingDays = travelAssistanceRequestDTO.getNumberOfStayingDays();
        String countryName = CountryCodesUtil.ISO_COUNTRIES.get(countryCodeDestination);

        return String.format("""
                You are a travel advisor helping a solo traveler understand visa and entry requirements.

                The traveler holds the following passports: %s
                They are planning to visit: %s
                Intended length of stay: %d days

                Please answer the following:

                1. Does the traveler need a visa to visit %s with any of their passports?
                2. Which passport offers the most favorable entry (e.g. visa-free or visa on arrival)?
                3. Are there any other entry requirements the traveler should be aware of? Include:
                   - Required or recommended vaccinations
                   - Travel insurance requirements
                   - Proof of onward travel
                   - COVID-19 related rules (if still applicable)
                4. Keep your answer concise and practical. Use bullet points.

                Assume this is for tourism only.
                """,
                String.join(", ", userProfile.getPassports()),
                countryName,
                numberOfStayingDays, countryName);
    }
}
