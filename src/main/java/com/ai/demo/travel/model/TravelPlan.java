package com.ai.demo.travel.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "travel_plans")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class TravelPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userProfileId;

    private String originCountry;

    @OneToMany(mappedBy = "travelPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<TravelPlanDestination> destinations;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private TripType tripType;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "travel_plan_traveler_types", joinColumns = @JoinColumn(name = "travel_plan_id"))
    @Column(name = "traveler_type")
    @Enumerated(EnumType.STRING)
    private List<TravelerType> travelerTypes;

    @Column(length = 1000)
    private String notes;

    @CreatedDate
    @EqualsAndHashCode.Exclude
    private LocalDateTime createdAt;

    @LastModifiedDate
    @EqualsAndHashCode.Exclude
    private LocalDateTime lastModifiedAt;

    public void prepareForCreation() {
        this.destinations.forEach(destination -> destination.mapTravelPlanRelationship(this));
    }
}
