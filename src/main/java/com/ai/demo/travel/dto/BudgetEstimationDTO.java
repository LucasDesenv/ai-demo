package com.ai.demo.travel.dto;

import com.ai.demo.travel.model.BudgetEstimationBreakdown;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class BudgetEstimationDTO {
    private Long id;

    private List<BudgetEstimationBreakdown> breakdowns;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    @JsonProperty("last_modified_at")
    private LocalDateTime lastModifiedAt;
}
