package com.ai.demo.finance.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ai.demo.finance.dto.RetirementDetailDTO;
import com.ai.demo.finance.dto.UserDTO;
import com.ai.demo.finance.event.retirement.RetirementEvent;
import com.ai.demo.finance.exception.NotFoundResourceException;
import com.ai.demo.finance.mapper.RetirementDetailMapper;
import com.ai.demo.finance.model.RetirementDetail;
import com.ai.demo.finance.model.repository.RetirementRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
public class RetirementServiceTest {

    public static final RetirementDetailMapper MAPPER = Mappers.getMapper(RetirementDetailMapper.class);
    @Mock
    private RetirementRepository retirementRepository;
    @Mock
    private UserService userService;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @InjectMocks
    private RetirementService retirementService;

    @Test
    void test_create_retirement_detail_success() {

        RetirementDetailDTO dto = new RetirementDetailDTO(1L, new BigDecimal("5000"), LocalDate.now().plusYears(50), LocalDate.now(), "user");
        RetirementDetail entity = MAPPER.toRetirementDetail(dto);

        when(retirementRepository.save(any(RetirementDetail.class))).thenReturn(entity);
        when(userService.findByUsername("user")).thenReturn(new UserDTO(2L, "user"));

        RetirementDetailDTO result = retirementService.createRetirementDetail(dto);

        assertNotNull(result);
        assertEquals(dto.id(), result.id());
        assertEquals(dto.incomePerMonthDesired(), result.incomePerMonthDesired());
        assertEquals(dto.lifeExpectation(), result.lifeExpectation());
        assertEquals(dto.retirementDate(), result.retirementDate());
        assertNull(result.username());
    }

    // Attempting to find a retirement detail with a non-existent ID
    @Test
    void test_find_retirement_detail_not_found() {

        when(retirementRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundResourceException.class, () -> retirementService.findById(1L));
    }

    // Finding an existing retirement detail by ID
    @Test
    void test_finding_existing_retirement_detail_by_id() {
        // Prepare
        Long id = 1L;
        RetirementDetailDTO expectedDetail = new RetirementDetailDTO(1L, new BigDecimal("5000"), LocalDate.now().plusYears(50), LocalDate.now(),
                null);
        when(retirementRepository.findById(id)).thenReturn(Optional.of(MAPPER.toRetirementDetail(expectedDetail)));

        // Execute
        RetirementDetailDTO result = retirementService.findById(id);

        // Verify
        assertEquals(expectedDetail, result);
    }

    // Successfully updates an existing retirement detail when the ID exists
    @Test
    void test_update_retirement_detail_success() {
        // Arrange
        Long id = 1L;
        RetirementDetailDTO dto = new RetirementDetailDTO(id, new BigDecimal("5000"), LocalDate.now().plusYears(50), LocalDate.now(), "user");
        long userId = 39L;
        RetirementDetail savedRetirementDetail = new RetirementDetail(id, new BigDecimal("5000"), LocalDate.now().plusYears(50), LocalDate.now(),
                userId);

        when(retirementRepository.existsById(id)).thenReturn(true);
        when(retirementRepository.save(any(RetirementDetail.class))).thenReturn(savedRetirementDetail);

        // Act
        RetirementDetailDTO result = retirementService.updateRetirementDetail(id, dto);

        // Assert
        assertNotNull(result);
        assertEquals(dto.id(), result.id());
        assertEquals(dto.incomePerMonthDesired(), result.incomePerMonthDesired());
        assertEquals(dto.lifeExpectation(), result.lifeExpectation());
        assertEquals(dto.retirementDate(), result.retirementDate());
        verify(eventPublisher).publishEvent(new RetirementEvent(userId));
    }

    // Throws NotFoundResourceException when the ID does not exist
    @Test
    void test_update_retirement_detail_not_found() {
        // Arrange
        Long id = 1L;
        RetirementDetailDTO dto = new RetirementDetailDTO(id, new BigDecimal("5000"), LocalDate.now().plusYears(50), LocalDate.now(), "user");

        when(retirementRepository.existsById(id)).thenReturn(false);

        // Act & Assert
        assertThrows(NotFoundResourceException.class, () -> retirementService.updateRetirementDetail(id, dto));
    }

    // Successfully deletes retirement detail when ID exists
    @Test
    void test_delete_retirement_detail_success() {

        Long id = 1L;
        when(retirementRepository.existsById(id)).thenReturn(true);

        retirementService.deleteRetirementDetail(id);

        verify(retirementRepository, times(1)).deleteById(id);
    }

    // Throws NotFoundResourceException when ID does not exist
    @Test
    void test_delete_retirement_detail_not_found() {
        Long id = 1L;
        when(retirementRepository.existsById(id)).thenReturn(false);

        assertThrows(NotFoundResourceException.class, () -> retirementService.deleteRetirementDetail(id));
    }

}
