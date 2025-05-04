package com.ai.demo.travel.dto;

import com.ai.demo.travel.validator.ValidCountryCode;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
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
    @JsonProperty("to_country")
    private String toCountry;
    @JsonProperty("to_city")
    @NotBlank
    private String toCity;

    @ValidCountryCode
    @NotBlank
    @JsonProperty("from_country")
    private String fromCountry;
    @JsonProperty("from_city")
    @NotBlank
    private String fromCity;

    @NotNull
    @JsonProperty("start_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @NotNull
    @JsonProperty("end_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    @JsonProperty("last_modified_at")
    private LocalDateTime lastModifiedAt;
}
