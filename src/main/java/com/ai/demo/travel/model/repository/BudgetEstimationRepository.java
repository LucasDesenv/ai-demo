package com.ai.demo.travel.model.repository;

import com.ai.demo.travel.model.BudgetEstimation;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BudgetEstimationRepository extends JpaRepository<BudgetEstimation, Long> {
    Optional<BudgetEstimation> findByTravelPlanId(Long travelPlanId);
}
