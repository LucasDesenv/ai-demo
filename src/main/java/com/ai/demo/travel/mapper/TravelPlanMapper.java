package com.ai.demo.travel.mapper;

import com.ai.demo.travel.dto.TravelPlanDTO;
import com.ai.demo.travel.model.TravelPlan;
import org.mapstruct.Mapper;

@Mapper
public interface TravelPlanMapper {

    TravelPlanDTO toDTO(TravelPlan entity);

    TravelPlan toEntity(TravelPlanDTO dto);
}
