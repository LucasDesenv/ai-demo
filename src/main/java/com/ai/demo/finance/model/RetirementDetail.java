package com.ai.demo.finance.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "retirement_detail")
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RetirementDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(name = "income_per_month_desired")
    private BigDecimal incomePerMonthDesired;
    @Column(name = "life_expcetation")
    private LocalDate lifeExpectation;
    @Column(name = "retirement_date")
    private LocalDate retirementDate;
    @Column(nullable = false, updatable = false, unique = true, name = "user_id")
    private Long userId;

}
