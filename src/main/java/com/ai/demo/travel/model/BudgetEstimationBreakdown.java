package com.ai.demo.travel.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "budget_estimation_breakdowns")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class BudgetEstimationBreakdown {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long travelPlanDestinationId;

    private BigDecimal estimation;

    private String notes;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_estimation_id")
    private BudgetEstimation budgetEstimation;

    @CreatedDate
    @EqualsAndHashCode.Exclude
    private LocalDateTime createdAt;

    @LastModifiedDate
    @EqualsAndHashCode.Exclude
    private LocalDateTime lastModifiedAt;

    void mapBudgetEstimation(BudgetEstimation budgetEstimation) {
        this.budgetEstimation = budgetEstimation;
    }

    public void updateNotes(String notes) {
        this.notes = notes;
    }
}
