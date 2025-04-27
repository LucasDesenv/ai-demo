package com.ai.demo.travel.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class Destination {
    private String country;
    private String city;
    private Long stayingDays;
}
