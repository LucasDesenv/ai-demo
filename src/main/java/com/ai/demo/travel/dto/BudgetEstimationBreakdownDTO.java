package com.ai.demo.travel.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BudgetEstimationBreakdownDTO {
    private Long id;

    private String notes;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    @JsonProperty("last_modified_at")
    private LocalDateTime lastModifiedAt;
}
