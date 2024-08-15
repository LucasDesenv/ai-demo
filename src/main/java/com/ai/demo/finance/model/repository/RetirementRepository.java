package com.ai.demo.finance.model.repository;

import com.ai.demo.finance.model.RetirementDetail;
import org.springframework.data.repository.CrudRepository;

public interface RetirementRepository extends CrudRepository<RetirementDetail, Long> {
}
