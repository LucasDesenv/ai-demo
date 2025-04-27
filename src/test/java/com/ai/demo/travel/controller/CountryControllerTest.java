package com.ai.demo.travel.controller;

import com.ai.demo.utils.CountryCodesUtil;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class CountryControllerTest {

    @Test
    void shouldReturnAllCountries() {
        CountryController countryController = new CountryController();
        Map<String, String> countries = countryController.getCountries();
        Assertions.assertThat(countries).isEqualTo(CountryCodesUtil.ISO_COUNTRIES);
    }
}
