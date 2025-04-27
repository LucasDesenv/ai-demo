package com.ai.demo.travel.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.ai.demo.travel.dto.DestinationDTO;
import com.ai.demo.travel.dto.TravelPlanDTO;
import com.ai.demo.travel.mapper.TravelPlanMapper;
import com.ai.demo.travel.model.Destination;
import com.ai.demo.travel.model.TravelPlan;
import com.ai.demo.travel.model.TripType;
import com.ai.demo.travel.model.repository.TravelPlanRepository;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class TravelPlanServiceTest {

    @Mock
    private TravelPlanRepository repository;

    private final TravelPlanMapper mapper = Mappers.getMapper(TravelPlanMapper.class);

    @InjectMocks
    private TravelPlanService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        service = new TravelPlanService(repository);
    }

    @Test
    void testSave_shouldStoreTravelPlan() {
        Long userId = 1L;
        TravelPlanDTO dto = TravelPlanDTO.builder()
                .destinations(Collections.singletonList(DestinationDTO.builder().stayingDays(15L).city("Lisbon").country("PT").build()))
                .startDate(LocalDate.of(2025, 6, 1))
                .endDate(LocalDate.of(2025, 6, 10))
                .tripType(TripType.VACATION)
                .notes("Looking for a relaxed summer trip")
                .build();

        TravelPlan entity = mapper.toEntity(dto);

        when(repository.save(any())).thenReturn(entity);

        TravelPlanDTO saved = service.save(userId, dto);

        assertEquals("PT", saved.getDestinations().get(0).getCountry());
        assertEquals("Lisbon", saved.getDestinations().get(0).getCity());

        verify(repository).save(any());
    }

    @Test
    void testFindAllByUser_shouldReturnPlansForUser() {
        Long userId = 2L;
        TravelPlan plan = TravelPlan.builder()
                .userProfileId(userId)
                .destinations(Collections.singletonList(Destination.builder().stayingDays(15L).city("Madrid").country("ES").build()))
                .startDate(LocalDate.of(2025, 4, 1))
                .endDate(LocalDate.of(2025, 4, 8))
                .tripType(TripType.WORK)
                .notes("Work conference")
                .build();

        when(repository.findByUserProfileId(userId)).thenReturn(List.of(plan));

        List<TravelPlanDTO> results = service.findAllByUser(userId);

        assertEquals(1, results.size());
        assertEquals("Madrid", results.get(0).getDestinations().get(0).getCity());
        assertEquals("ES", results.get(0).getDestinations().get(0).getCountry());
    }
}
