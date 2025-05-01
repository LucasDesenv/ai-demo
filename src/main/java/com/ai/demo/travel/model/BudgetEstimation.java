package com.ai.demo.travel.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
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

@Entity
@Table(name = "budget_estimations")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class BudgetEstimation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long travelPlanId;

    private BigDecimal totalEstimation;

    @OneToMany(mappedBy = "budgetEstimation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BudgetEstimationBreakdown> breakdowns;

    @CreatedDate
    @EqualsAndHashCode.Exclude
    private LocalDateTime createdAt;

    @LastModifiedDate
    @EqualsAndHashCode.Exclude
    private LocalDateTime lastModifiedAt;

    public void prepareForCreation() {
        this.breakdowns.forEach(bd -> bd.mapBudgetEstimation(this));
    }

    public void updateBreakDowns(List<BudgetEstimationBreakdown> newBreakDowns) {
        this.breakdowns = newBreakDowns;
    }
}
