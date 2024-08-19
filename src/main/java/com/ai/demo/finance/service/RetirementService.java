package com.ai.demo.finance.service;

import com.ai.demo.finance.dto.RetirementDetailDTO;
import com.ai.demo.finance.dto.UserDTO;
import com.ai.demo.finance.event.retirement.RetirementEvent;
import com.ai.demo.finance.exception.NotFoundResourceException;
import com.ai.demo.finance.mapper.RetirementDetailMapper;
import com.ai.demo.finance.model.RetirementDetail;
import com.ai.demo.finance.model.repository.RetirementRepository;
import lombok.AllArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RetirementService {

    private static final RetirementDetailMapper MAPPER = Mappers.getMapper(RetirementDetailMapper.class);
    private final RetirementRepository retirementRepository;
    private final UserService userService;
    private final ApplicationEventPublisher eventPublisher;

    public RetirementDetailDTO createRetirementDetail(RetirementDetailDTO retirementDetail) {
        UserDTO user = userService.findByUsername(retirementDetail.username());
        RetirementDetail entity = MAPPER.toRetirementDetail(retirementDetail, user.id());
        return MAPPER.toRetirementDetailDTO(retirementRepository.save(entity));
    }

    public RetirementDetailDTO findById(Long id) {
        return retirementRepository.findById(id)
                .map(MAPPER::toRetirementDetailDTO)
                .orElseThrow(() -> new NotFoundResourceException("Retirement not found"));
    }

    public RetirementDetailDTO updateRetirementDetail(Long id, RetirementDetailDTO dto) {
        if (!retirementRepository.existsById(id)) {
            throw new NotFoundResourceException("RetirementDetail not found with id " + id);
        }

        RetirementDetail retirementDetail = MAPPER.toRetirementDetail(dto);
        RetirementDetail saved = retirementRepository.save(retirementDetail);
        eventPublisher.publishEvent(new RetirementEvent(saved.getUserId()));
        return MAPPER.toRetirementDetailDTO(saved);

    }

    public void deleteRetirementDetail(Long id) {
        if (!retirementRepository.existsById(id)) {
            throw new NotFoundResourceException("RetirementDetail not found with id " + id);
        }

        retirementRepository.deleteById(id);

    }

}
