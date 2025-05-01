package com.ai.demo.travel.mapper;

import com.ai.demo.travel.dto.BudgetEstimationDTO;
import com.ai.demo.travel.model.BudgetEstimation;
import org.mapstruct.Mapper;

@Mapper
public interface BudgetEstimationMapper {
    BudgetEstimationDTO toDTO(BudgetEstimation entity);
}
