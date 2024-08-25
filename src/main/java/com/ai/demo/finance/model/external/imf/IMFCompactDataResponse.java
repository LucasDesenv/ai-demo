package com.ai.demo.finance.model.external.imf;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class IMFCompactDataResponse {
    @JsonProperty("CompactData")
    private CompactData compactData;
}
