package com.ai.demo.finance.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;

public record RetirementDetailDTO(
        @JsonProperty("id") Long id,
        @JsonProperty("incomePerMonthDesired") BigDecimal incomePerMonthDesired,
        @JsonProperty("lifeExpectation") Integer lifeExpectation,
        @JsonProperty("retirementDate") LocalDate retirementDate) {
}
