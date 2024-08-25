package com.ai.demo.finance.model.cache;

import static java.math.RoundingMode.HALF_UP;

import com.ai.demo.finance.model.enums.Country;
import com.ai.demo.finance.model.external.imf.Indicator;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class InflationRate implements Serializable {
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    private static final int SCALE = 2;
    private BigDecimal percentageRate;
    private String period;
    private Country country;
    private Indicator indicator;

    /**
     * Calculates the rate as fixed number from the percentageRate. Formula: 1 -
     * (percentageRate / 100) Example: 1% inflation rate would result in 0,99.
     * @return rate
     */
    public BigDecimal calculateRateFromPercentage() {
        return BigDecimal.ONE.subtract(this.getPercentageRate().divide(ONE_HUNDRED, SCALE, HALF_UP));
    }
}
