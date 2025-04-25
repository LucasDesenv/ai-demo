package com.ai.demo.travel.model.repository;

import com.ai.demo.travel.model.TravelPlan;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TravelPlanRepository extends JpaRepository<TravelPlan, Long> {
    List<TravelPlan> findByUserProfileId(Long userProfileId);
}