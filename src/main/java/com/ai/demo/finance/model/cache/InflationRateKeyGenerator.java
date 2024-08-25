package com.ai.demo.finance.model.cache;

import com.ai.demo.finance.model.enums.Country;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class InflationRateKeyGenerator {

    private static final String PREFIX = "INFLATION";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

    private InflationRateKeyGenerator() {

    }

    public static String generateKey(Country country, String period) {
        return PREFIX + "|" + country.name() + "|" + period;
    }

    public static String generateKey(InflationRate inflationRate) {
        return generateKey(inflationRate.getCountry(), inflationRate.getPeriod());
    }

    public static String generateKey(Country country, LocalDate date) {
        return generateKey(country, date.format(formatter));
    }
}
