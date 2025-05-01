package com.ai.demo.travel.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class AIBudgetResponse {
    private List<AIBudgetCostResponse> costs;
    @JsonProperty("total_estimated")
    private BigDecimal totalEstimated;
}
