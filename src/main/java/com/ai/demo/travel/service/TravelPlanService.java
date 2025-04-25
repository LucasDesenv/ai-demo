package com.ai.demo.travel.service;

import com.ai.demo.travel.dto.TravelPlanDTO;
import com.ai.demo.travel.mapper.TravelPlanMapper;
import com.ai.demo.travel.model.TravelPlan;
import com.ai.demo.travel.model.repository.TravelPlanRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TravelPlanService {

    private final TravelPlanRepository repository;
    private final TravelPlanMapper mapper = Mappers.getMapper(TravelPlanMapper.class);

    public TravelPlanDTO save(Long userId, TravelPlanDTO dto) {
        dto.setUserProfileId(userId);
        TravelPlan entity = mapper.toEntity(dto);
        return mapper.toDTO(repository.save(entity));
    }

    public List<TravelPlanDTO> findAllByUser(Long userId) {
        return repository.findByUserProfileId(userId).stream()
                .map(mapper::toDTO)
                .toList();
    }
}