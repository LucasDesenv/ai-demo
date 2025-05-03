package com.ai.demo.travel.helper;

import com.ai.demo.travel.dto.UserProfileDTO;
import com.ai.demo.travel.model.BudgetLevel;

public class UserProfileHelper {

    public static UserProfileDTO defaultUserProfile(Long userId) {
        return UserProfileDTO.builder().id(userId).budgetLevel(BudgetLevel.MEDIUM).travelStyle("Backpacker")
                .build();
    }
}
