package com.ai.demo.travel.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BudgetBreakdownCostDTO {
    private Long id;

    private BigDecimal estimation;

    private String description;

    private String notes;
}
