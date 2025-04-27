package com.ai.demo.travel.controller;

import com.ai.demo.utils.CountryCodesUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(headers = {TravelApiVersion.TRAVEL_ACCEPT_VERSION + "=" + TravelApiVersion.TRAVEL_API_V1})
@RequiredArgsConstructor
@Tag(name = "ISO Countries", description = "APIs related to Countries")
public class CountryController {

    public static final String ENDPOINT = "/countries";

    @GetMapping(ENDPOINT)
    public Map<String, String> getCountries() {
        return CountryCodesUtil.ISO_COUNTRIES;
    }
}
