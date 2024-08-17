package com.ai.demo.finance.dto;

import com.ai.demo.finance.model.enums.AccountType;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountDTO(
        @JsonProperty("id") Long id,
        @JsonProperty("amount") BigDecimal amount,
        @JsonProperty("type") AccountType type,
        @JsonProperty("date") LocalDateTime date,
        @JsonProperty("username") String username) {
}
