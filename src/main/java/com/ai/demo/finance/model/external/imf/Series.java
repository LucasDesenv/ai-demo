package com.ai.demo.finance.model.external.imf;

import com.ai.demo.finance.model.enums.Country;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Stack;
import lombok.Data;

@Data
public class Series {
    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    private static final BigDecimal ONE_HUNDRED_PERCENT = BigDecimal.valueOf(100);

    @JsonProperty("@FREQ")
    private String frequency;

    @JsonProperty("@REF_AREA")
    private Country country;

    @JsonProperty("@INDICATOR")
    private Indicator indicator;

    @JsonProperty("@UNIT_MULT")
    private String unitMult;

    @JsonProperty("@BASE_YEAR")
    private String baseYear;

    @JsonProperty("@TIME_FORMAT")
    private String timeFormat;

    @JsonProperty("Obs")
    private Stack<RatePeriod> ratePeriods;

    /**
     * Formula: (CPI_Current−CPI_Previous)/(CPI_Previous)*100
     * @return new com.ai.demo.finance.model.cache.InflationRate
     */
    @JsonIgnore
    public RatePeriod calculateMonthInflationRate() {
        if (ratePeriods == null || ratePeriods.isEmpty()) {
            throw new IllegalStateException("No RatePeriods found");
        }

        RatePeriod lastMonth = ratePeriods.pop();

        if (ratePeriods.isEmpty()) {
            return lastMonth;
        }

        RatePeriod previous = ratePeriods.pop();

        BigDecimal rate = lastMonth.getRate().subtract(previous.getRate())
                .divide(previous.getRate(), SCALE, ROUNDING_MODE).multiply(ONE_HUNDRED_PERCENT).setScale(SCALE, ROUNDING_MODE);

        return RatePeriod.builder()
                .rate(rate)
                .timePeriod(lastMonth.getTimePeriod())
                .build();
    }
}
