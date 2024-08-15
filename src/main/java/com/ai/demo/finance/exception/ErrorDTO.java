package com.ai.demo.finance.exception;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic error dto.
 * @author lolo on 2022-06-06
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder(value = {"code", "message"})
@JsonTypeName("error")
public class ErrorDTO {
    @JsonProperty(value = "code")
    private String code;

    @JsonProperty(value = "message")
    private String message;
}
