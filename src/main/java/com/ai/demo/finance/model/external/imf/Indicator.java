package com.ai.demo.finance.model.external.imf;

import lombok.Getter;

@Getter
public enum Indicator {
    PCPIPCH("Inflation rate, average consumer prices."),
    PCPIEPCH("Inflation rate, end of period consumer prices."),
    PCPI_IX("Consumer Price Index, All Items");

    private final String description;

    Indicator(String description) {
        this.description = description;
    }

}
