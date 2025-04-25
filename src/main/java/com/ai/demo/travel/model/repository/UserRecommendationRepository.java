package com.ai.demo.travel.model.repository;

import com.ai.demo.travel.model.UserRecommendation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRecommendationRepository extends JpaRepository<UserRecommendation, Long> {
    List<UserRecommendation> findByUserProfileId(Long userId);
}
