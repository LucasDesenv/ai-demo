package com.ai.demo.finance.model.external.imf;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RatePeriod {
    @JsonProperty("@TIME_PERIOD")
    private String timePeriod;

    @JsonProperty("@OBS_VALUE")
    private BigDecimal rate;
}
