package com.ai.demo.finance.mapper;

import com.ai.demo.finance.dto.RetirementDetailDTO;
import com.ai.demo.finance.model.RetirementDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface RetirementDetailMapper {

    RetirementDetailDTO toRetirementDetailDTO(RetirementDetail retirementDetail);

    RetirementDetail toRetirementDetail(RetirementDetailDTO retirementDetailDTO);

    @Mapping(target = "userId", source = "userId")
    RetirementDetail toRetirementDetail(RetirementDetailDTO retirementDetailDTO, Long userId);
}
