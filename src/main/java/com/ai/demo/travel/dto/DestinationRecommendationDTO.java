package com.ai.demo.travel.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DestinationRecommendationDTO {

    private String city;
    private String country;
    private List<String> tags;
    private String reason;

}
