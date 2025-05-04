package com.ai.demo.travel.dto;

import com.ai.demo.travel.model.TravelerType;
import com.ai.demo.travel.model.TripType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @NotEmpty
    @Valid
    @Size(min = 1, max = 5)
    private List<TravelPlanDestinationDTO> destinations;

    @NotEmpty
    @Size(min = 1, max = 20)
    @JsonProperty("traveler_types")
    private List<TravelerType> travelerTypes;

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

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    @JsonProperty("last_modified_at")
    private LocalDateTime lastModifiedAt;
}