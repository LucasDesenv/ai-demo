package com.ai.demo.finance.model.repository;

import com.ai.demo.finance.model.RetirementDetail;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RetirementRepository extends JpaRepository<RetirementDetail, Long> {

    Optional<RetirementDetail> findByUserId(Long userId);
}
