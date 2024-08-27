package com.ai.demo.finance.dto;

import com.ai.demo.finance.model.enums.AccountType;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record AccountDTO(
        @JsonProperty("id") Long id,
        @JsonProperty("description") String description,
        @JsonProperty("amount") BigDecimal amount,
        @JsonProperty("type") AccountType type,
        @JsonProperty("username") String username) {
}
