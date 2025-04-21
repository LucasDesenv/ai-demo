package com.ai.demo.travel.model.repository;

import com.ai.demo.travel.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
}
