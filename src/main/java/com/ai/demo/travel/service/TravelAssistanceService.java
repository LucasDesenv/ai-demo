package com.ai.demo.travel.service;

import com.ai.demo.finance.ai.ChatGptService;
import com.ai.demo.travel.dto.TravelAssistanceRequestDTO;
import com.ai.demo.travel.dto.UserProfileDTO;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@AllArgsConstructor
public class TravelAssistanceService {
    private final ChatGptService chatGptService;
    private final UserProfileService userProfileService;

    public String recommendDestinations(Long userProfileId) {
        UserProfileDTO userProfile = userProfileService.findById(userProfileId);
        return chatGptService.ask(buildPromptForRecommendations(userProfile));
    }

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

    public String requirements(String countryDestination, TravelAssistanceRequestDTO travelAssistanceRequestDTO) {
        UserProfileDTO userProfile = userProfileService.findById(travelAssistanceRequestDTO.getUserId());

        List<String> passports = userProfile.getPassports();

        if (CollectionUtils.isEmpty(passports)) {
            return "The user does not hold any passport. User needs to provide at least their ID as passport.";
        }

        if (passports.contains(countryDestination)) {
            return String.format("The user holds a passport of the destination: %s", countryDestination);
        }

        String prompt = buildPromptForRequirements(userProfile, countryDestination, travelAssistanceRequestDTO);

        return chatGptService.ask(prompt);
    }

    private String buildPromptForRequirements(UserProfileDTO userProfile,
            String countryDestination, TravelAssistanceRequestDTO travelAssistanceRequestDTO) {

        Integer numberOfStayingDays = travelAssistanceRequestDTO.getNumberOfStayingDays();

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
                countryDestination,
                numberOfStayingDays, countryDestination);
    }
}
