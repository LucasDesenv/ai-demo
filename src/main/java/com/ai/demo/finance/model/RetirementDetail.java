package com.ai.demo.finance.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "retirement_detail")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RetirementDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private BigDecimal incomePerMonthDesired;
    private Integer lifeExpectation;
    private LocalDate retirementDate;
    @Column(nullable = false, updatable = false, unique = true)
    private Long userId;

}
