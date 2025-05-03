package com.ai.demo.travel.helper;

import com.ai.demo.travel.model.TravelPlan;
import com.ai.demo.travel.model.TravelPlanDestination;
import com.ai.demo.travel.model.TripType;
import java.time.LocalDate;
import java.util.ArrayList;

public class TravelPlanHelper {
    public static TravelPlan withSingleDestination(Long userProfileId) {
        LocalDate startDate = LocalDate.now().plusDays(5);
        LocalDate endDate = LocalDate.now().plusDays(10);

        TravelPlan travelPlan = TravelPlan.builder().userProfileId(userProfileId).tripType(TripType.VACATION)
                .startDate(startDate).endDate(endDate).originCountry("BR")
                .destinations(new ArrayList<>())
                .build();

        var destination = TravelPlanDestination.builder()
                .country("DE")
                .city("Berlin")
                .startDate(startDate)
                .endDate(endDate)
                .travelPlan(travelPlan)
                .build();

        travelPlan.getDestinations().add(destination);

        return travelPlan;
    }
}
