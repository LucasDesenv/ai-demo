package com.ai.demo.finance.model.enums;

import lombok.Getter;

@Getter
public enum Country {
    ES("Spain"),
    US("United States of America"),
    BR("Brazil");

    private final String description;

    Country(String description) {

        this.description = description;
    }
}
