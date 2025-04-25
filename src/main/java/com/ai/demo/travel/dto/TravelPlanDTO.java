package com.ai.demo.travel.dto;

import com.ai.demo.travel.model.TripType;
import com.ai.demo.travel.validator.ValidCountryCode;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TravelPlanDTO {
    private Long id;

    @JsonProperty("user_profile_id")
    private Long userProfileId;

    @Valid
    @JsonProperty("destination_countries")
    @NotEmpty
    private List<@ValidCountryCode String> destinationCountries;

    @JsonProperty("destination_cities")
    private List<String> destinationCities;

    @JsonProperty("start_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonProperty("end_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @JsonProperty("trip_type")
    @NotNull
    private TripType tripType;
    @Size(max = 1000)
    private String notes;
}