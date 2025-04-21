package com.ai.demo.travel.service;

import com.ai.demo.finance.ai.ChatGptService;
import com.ai.demo.travel.dto.UserProfileDTO;
import java.time.LocalDate;
import java.time.Period;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RecommendationService {
    private final ChatGptService chatGptService;
    private final UserProfileService userProfileService;

    public String recommend(Long userProfileId) {
        UserProfileDTO userProfile = userProfileService.findById(userProfileId);
        return chatGptService.ask(buildPrompt(userProfile));
    }

    private String buildPrompt(UserProfileDTO profile) {
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

}
