package com.ai.demo.travel.dto;

import com.ai.demo.travel.validator.ValidCountryCode;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TravelPlanDestinationDTO {

    private Long id;

    @ValidCountryCode
    @NotBlank
    private String country;
    @NotBlank
    private String city;
    @NotNull
    @JsonProperty("staying_days")
    private Long stayingDays;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    @JsonProperty("last_modified_at")
    private LocalDateTime lastModifiedAt;
}
