package com.ai.demo.finance.model.external.imf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.Stack;
import org.junit.jupiter.api.Test;

class SeriesTest {

    // Calculates the inflation rate correctly for two valid RatePeriod objects
    @Test
    void test_calculate_inflation_rate_correctly() {
        Stack<RatePeriod> ratePeriods = new Stack<>();
        ratePeriods.push(RatePeriod.builder().rate(new BigDecimal("100")).timePeriod("2023-08").build());
        ratePeriods.push(RatePeriod.builder().rate(new BigDecimal("110")).timePeriod("2023-09").build());

        Series series = new Series();
        series.setRatePeriods(ratePeriods);

        RatePeriod result = series.calculateMonthInflationRate();

        assertEquals(new BigDecimal("10.00"), result.getRate());
        assertEquals("2023-09", result.getTimePeriod());
    }

    // Handles the case where ratePeriods stack has less than two elements
    @Test
    void test_handle_insufficient_rate_periods() {
        Stack<RatePeriod> ratePeriods = new Stack<>();
        ratePeriods.push(RatePeriod.builder().rate(new BigDecimal("100")).timePeriod("2023-08").build());

        Series series = new Series();
        series.setRatePeriods(ratePeriods);

        RatePeriod result = series.calculateMonthInflationRate();

        assertEquals(new BigDecimal("100"), result.getRate());
        assertEquals("2023-08", result.getTimePeriod());
    }

    // Handles the case where ratePeriods stack is null
    @Test
    void test_handle_null_rate_periods() {
        Stack<RatePeriod> ratePeriods = null;

        Series series = new Series();
        series.setRatePeriods(ratePeriods);

        assertThrows(IllegalStateException.class, series::calculateMonthInflationRate);
    }

    // Handles the case where ratePeriods stack is empty
    @Test
    void test_handle_empty_rate_periods() {
        Stack<RatePeriod> ratePeriods = new Stack<>();

        Series series = new Series();
        series.setRatePeriods(ratePeriods);

        assertThrows(IllegalStateException.class, series::calculateMonthInflationRate);
    }
}
