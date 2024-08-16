package com.ai.demo.finance.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

@Valid
public record UserDTO(
        @JsonProperty("id") Long id,

        @Valid @JsonProperty("username") @NotEmpty @Size(min = 3, max = 15) String username) {
}