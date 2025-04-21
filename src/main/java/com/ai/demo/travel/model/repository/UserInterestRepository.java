package com.ai.demo.travel.model.repository;

import com.ai.demo.travel.model.UserInterest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserInterestRepository extends JpaRepository<UserInterest, Long> {
}
