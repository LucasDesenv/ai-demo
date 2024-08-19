package com.ai.demo.finance.model.repository;

import com.ai.demo.finance.model.RetirementDetail;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface RetirementRepository extends CrudRepository<RetirementDetail, Long> {

    Optional<RetirementDetail> findByUserId(Long userId);
}
