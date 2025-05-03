package com.ai.demo.travel.helper;

import com.ai.demo.travel.dto.TravelPlanDTO;
import com.ai.demo.travel.dto.TravelPlanDestinationDTO;
import com.ai.demo.travel.model.TravelPlan;
import com.ai.demo.travel.model.TravelPlanDestination;
import com.ai.demo.travel.model.TravelerType;
import com.ai.demo.travel.model.TripType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    public static TravelPlanDTO dtoWithSingleDestination(Long userProfileId, Long travelPlanId, Long destinationId) {
        LocalDate start = LocalDate.now().plusDays(5);
        LocalDate end = LocalDate.now().plusDays(10);

        return TravelPlanDTO.builder()
                .id(travelPlanId)
                .userProfileId(userProfileId)
                .tripType(TripType.VACATION)
                .originCountry("BR")
                .startDate(start)
                .endDate(end)
                .lastModifiedAt(LocalDateTime.now())
                .destinations(new ArrayList<>(List.of(TravelPlanDestinationDTO.builder()
                        .id(destinationId)
                        .city("Berlin")
                        .country("DE")
                        .startDate(start)
                        .endDate(end)
                        .lastModifiedAt(LocalDateTime.now())
                        .build())))
                .travelerTypes(List.of(TravelerType.ADULT))
                .build();
    }

}
