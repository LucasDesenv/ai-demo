package com.ai.demo.travel.model;

import com.ai.demo.travel.dto.AIBudgetResponse;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @Column(name = "travel_plan_destination_id", nullable = false, insertable = true, updatable = false)
    private Long travelPlanDestinationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_plan_destination_id", insertable = false, updatable = false)
    private TravelPlanDestination travelPlanDestination;

    private BigDecimal estimation;

    private String notes;

    @Builder.Default
    @OneToMany(mappedBy = "budgetEstimationBreakdown", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BudgetBreakdownCost> costs = new ArrayList<>();

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

    public void replaceCosts(AIBudgetResponse aiBudgetResponse) {
        this.estimation = aiBudgetResponse.getTotalEstimated();
        this.costs.clear();
        aiBudgetResponse.getCosts().forEach(cost -> this.costs.add(BudgetBreakdownCost.builder()
                .estimation(cost.getCost())
                .notes(cost.getNotes())
                .description(cost.getDescription())
                .budgetEstimationBreakdown(this)
                .build()));
    }
}
