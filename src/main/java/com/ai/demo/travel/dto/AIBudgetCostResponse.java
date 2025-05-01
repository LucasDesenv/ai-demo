package com.ai.demo.travel.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class AIBudgetCostResponse {
    private String description;
    private BigDecimal cost;
    private String notes;
}
