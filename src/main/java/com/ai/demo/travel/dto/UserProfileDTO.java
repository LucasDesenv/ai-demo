package com.ai.demo.travel.dto;

import com.ai.demo.travel.model.BudgetLevel;
import com.ai.demo.travel.model.Gender;
import com.ai.demo.travel.validator.ValidCountryCode;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserProfileDTO {
    private Long id;
    @NotBlank
    private String name;
    @NotNull
    private LocalDate birth;
    @NotNull
    private Gender gender;
    @NotNull
    @JsonProperty("budget_level")
    private BudgetLevel budgetLevel;
    @JsonProperty("preferred_climates")
    @NotEmpty
    private List<String> preferredClimates;
    @NotEmpty
    @JsonProperty("languages_spoken")
    private List<String> languagesSpoken;
    @NotEmpty
    @JsonProperty("travel_style")
    private String travelStyle;
    @NotEmpty
    private List<String> interests;
    @Valid
    @JsonProperty("passports")
    private List<@ValidCountryCode String> passports;
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    @JsonProperty("last_modified_at")
    private LocalDateTime lastModifiedAt;

}
