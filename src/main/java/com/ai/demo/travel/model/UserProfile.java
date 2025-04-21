package com.ai.demo.travel.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_travel_profiles")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class UserProfile {

    @Id
    @GeneratedValue
    private Long id;

    private String name;
    private LocalDate birth;
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    private BudgetLevel budgetLevel;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_preferred_climates", joinColumns = @JoinColumn(name = "user_profile_id"))
    @Column(name = "climate")
    private List<String> preferredClimates;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_languages", joinColumns = @JoinColumn(name = "user_profile_id"))
    @Column(name = "language")
    private List<String> languagesSpoken;

    private String travelStyle;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<UserInterest> interests = new ArrayList<>();

}
