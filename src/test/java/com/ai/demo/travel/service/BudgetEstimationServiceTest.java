package com.ai.demo.travel.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.ai.demo.finance.ai.ChatGptService;
import com.ai.demo.finance.exception.InvalidOperationException;
import com.ai.demo.travel.dto.TravelPlanDTO;
import com.ai.demo.travel.dto.TravelPlanDestinationDTO;
import com.ai.demo.travel.dto.UserProfileDTO;
import com.ai.demo.travel.model.BudgetEstimation;
import com.ai.demo.travel.model.BudgetEstimationBreakdown;
import com.ai.demo.travel.model.BudgetLevel;
import com.ai.demo.travel.model.repository.BudgetEstimationRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BudgetEstimationServiceTest {

    @Mock
    private UserProfileService userProfileService;

    @Mock
    private TravelPlanService travelPlanService;

    @Mock
    private ChatGptService chatGptService;

    @Mock
    private BudgetEstimationRepository budgetEstimationRepository;

    @InjectMocks
    private BudgetEstimationService budgetEstimationService;

    @Test
    void estimateBudget_shouldCreateNewBudgetEstimation_whenNoneExists() {
        Long userId = 1L;
        Long travelPlanId = 2L;

        TravelPlanDTO travelPlan = TravelPlanDTO.builder()
                .id(travelPlanId)
                .startDate(LocalDate.now())
                .originCountry("BR")
                .endDate(LocalDate.now().plusDays(10))
                .destinations(List.of(TravelPlanDestinationDTO.builder().id(10L).country("FR").city("Paris").stayingDays(5L)
                        .lastModifiedAt(LocalDateTime.now()).build()))
                .build();

        UserProfileDTO userProfile = UserProfileDTO.builder()
                .id(userId)
                .budgetLevel(BudgetLevel.MEDIUM)
                .travelStyle("Backpacker")
                .build();

        when(travelPlanService.findById(travelPlanId)).thenReturn(travelPlan);
        when(userProfileService.findById(userId)).thenReturn(userProfile);
        when(budgetEstimationRepository.findByTravelPlanId(travelPlanId)).thenReturn(Optional.empty());
        when(chatGptService.ask(any())).thenReturn("Sample breakdown from AI");
        when(budgetEstimationRepository.save(any(BudgetEstimation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = budgetEstimationService.estimateBudget(userId, travelPlanId);

        assertNotNull(result);
        assertEquals(1, result.getBreakdowns().size());
        verify(budgetEstimationRepository, times(1)).save(any(BudgetEstimation.class));
    }

    @Test
    void estimateBudget_shouldThrowException_whenTravelPlanStartDateIsMissing() {
        Long userId = 1L;
        Long travelPlanId = 2L;

        TravelPlanDTO incompleteTravelPlan = TravelPlanDTO.builder()
                .id(travelPlanId)
                .startDate(null)
                .endDate(LocalDate.now())
                .build();

        when(travelPlanService.findById(travelPlanId)).thenReturn(incompleteTravelPlan);

        InvalidOperationException ex = assertThrows(InvalidOperationException.class,
                () -> budgetEstimationService.estimateBudget(userId, travelPlanId));

        assertEquals("Travel plan is not completed. Dates are required.", ex.getMessage());
        verifyNoInteractions(userProfileService);
        verifyNoInteractions(budgetEstimationRepository);
    }

    @Test
    void estimateBudget_shouldThrowException_whenTravelPlanEndDateIsMissing() {
        Long userId = 1L;
        Long travelPlanId = 2L;

        TravelPlanDTO incompleteTravelPlan = TravelPlanDTO.builder()
                .id(travelPlanId)
                .startDate(LocalDate.now())
                .endDate(null)
                .build();

        when(travelPlanService.findById(travelPlanId)).thenReturn(incompleteTravelPlan);

        InvalidOperationException ex = assertThrows(InvalidOperationException.class,
                () -> budgetEstimationService.estimateBudget(userId, travelPlanId));

        assertEquals("Travel plan is not completed. Dates are required.", ex.getMessage());
        verifyNoInteractions(userProfileService);
        verifyNoInteractions(budgetEstimationRepository);
    }

    @Test
    void estimateBudget_shouldReuseExistingBudget_whenNoDestinationChanges() {
        Long userId = 1L;
        Long travelPlanId = 2L;
        LocalDateTime now = LocalDateTime.now();

        TravelPlanDTO travelPlan = TravelPlanDTO.builder()
                .id(travelPlanId)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(10))
                .lastModifiedAt(now)
                .destinations(List.of(
                        TravelPlanDestinationDTO.builder()
                                .id(10L)
                                .country("FR")
                                .city("Paris")
                                .stayingDays(5L)
                                .lastModifiedAt(now)
                                .build()))
                .build();

        BudgetEstimation existingBudget = BudgetEstimation.builder()
                .id(100L)
                .travelPlanId(travelPlanId)
                .lastModifiedAt(now.plusMinutes(-1))
                .breakdowns(new ArrayList<>(List.of(
                        BudgetEstimationBreakdown.builder()
                                .travelPlanDestinationId(10L)
                                .createdAt(now)
                                .lastModifiedAt(now)
                                .build())))
                .build();

        when(travelPlanService.findById(travelPlanId)).thenReturn(travelPlan);
        when(budgetEstimationRepository.findByTravelPlanId(travelPlanId)).thenReturn(Optional.of(existingBudget));

        var result = budgetEstimationService.estimateBudget(userId, travelPlanId);

        assertNotNull(result);
        assertEquals(existingBudget.getId(), result.getId());
        verify(budgetEstimationRepository, never()).save(any());
        verifyNoInteractions(chatGptService); // No AI call needed
    }

    @Test
    void estimateBudget_shouldReuseExistingBudget_whenNewDestinationAdded() {
        Long userId = 1L;
        Long travelPlanId = 2L;
        LocalDateTime now = LocalDateTime.now();

        TravelPlanDTO travelPlan = TravelPlanDTO.builder()
                .id(travelPlanId)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(10))
                .lastModifiedAt(now)
                .originCountry("US")
                .destinations(List.of(
                        TravelPlanDestinationDTO.builder()
                                .id(10L)
                                .country("FR")
                                .city("Paris")
                                .stayingDays(5L)
                                .lastModifiedAt(now)
                                .build(),
                        TravelPlanDestinationDTO.builder()
                                .id(938723L)
                                .country("CA")
                                .city("NEW ONE")
                                .stayingDays(7L)
                                .lastModifiedAt(now)
                                .build()))
                .build();

        BudgetEstimation existingBudget = BudgetEstimation.builder()
                .id(100L)
                .travelPlanId(travelPlanId)
                .lastModifiedAt(now.plusMinutes(-1))
                .breakdowns(new ArrayList<>(List.of(
                        BudgetEstimationBreakdown.builder()
                                .travelPlanDestinationId(10L)
                                .createdAt(now)
                                .lastModifiedAt(now)
                                .build())))
                .build();

        UserProfileDTO userProfile = UserProfileDTO.builder()
                .id(userId)
                .budgetLevel(BudgetLevel.MEDIUM)
                .travelStyle("Backpacker")
                .build();

        when(userProfileService.findById(userId)).thenReturn(userProfile);
        when(travelPlanService.findById(travelPlanId)).thenReturn(travelPlan);
        when(budgetEstimationRepository.findByTravelPlanId(travelPlanId)).thenReturn(Optional.of(existingBudget));
        when(chatGptService.ask(any())).thenReturn("New breakdown after update");
        when(budgetEstimationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = budgetEstimationService.estimateBudget(userId, travelPlanId);

        assertNotNull(result);
        assertEquals(2, result.getBreakdowns().size());
        verify(chatGptService, times(1)).ask(any());
        verify(budgetEstimationRepository, times(1)).save(any());
    }

    @Test
    void estimateBudget_shouldUpdateBudget_whenDestinationsModified() {
        Long userId = 1L;
        Long travelPlanId = 2L;
        LocalDateTime now = LocalDateTime.now();

        TravelPlanDTO travelPlan = TravelPlanDTO.builder()
                .id(travelPlanId)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(10))
                .lastModifiedAt(now.plusHours(2))
                .originCountry("BR")
                .destinations(List.of(
                        TravelPlanDestinationDTO.builder()
                                .id(10L)
                                .country("FR")
                                .city("Paris")
                                .stayingDays(5L)
                                .lastModifiedAt(now.plusHours(1)) // more recent than previous budget
                                .build()))
                .build();

        BudgetEstimation oldBudget = BudgetEstimation.builder()
                .id(100L)
                .travelPlanId(travelPlanId)
                .lastModifiedAt(now) // older
                .breakdowns(new ArrayList<>(List.of(
                        BudgetEstimationBreakdown.builder()
                                .travelPlanDestinationId(10L)
                                .createdAt(now)
                                .lastModifiedAt(now)
                                .build())))
                .build();

        UserProfileDTO userProfile = UserProfileDTO.builder()
                .id(userId)
                .budgetLevel(BudgetLevel.MEDIUM)
                .travelStyle("Backpacker")
                .build();

        when(travelPlanService.findById(travelPlanId)).thenReturn(travelPlan);
        when(userProfileService.findById(userId)).thenReturn(userProfile);
        when(budgetEstimationRepository.findByTravelPlanId(travelPlanId)).thenReturn(Optional.of(oldBudget));
        when(chatGptService.ask(any())).thenReturn("New breakdown after update");
        when(budgetEstimationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = budgetEstimationService.estimateBudget(userId, travelPlanId);

        assertNotNull(result);
        assertEquals(1, result.getBreakdowns().size());
        verify(chatGptService, times(1)).ask(any());
        verify(budgetEstimationRepository, times(1)).save(any());
    }

    @Test
    void estimateBudget_shouldThrowIfBudgetIsStillFresh() {
        Long userId = 1L;
        Long travelPlanId = 2L;
        LocalDateTime now = LocalDateTime.now();

        TravelPlanDTO travelPlan = TravelPlanDTO.builder()
                .id(travelPlanId)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(10))
                .lastModifiedAt(now)
                .originCountry("BR")
                .destinations(List.of(
                        TravelPlanDestinationDTO.builder()
                                .id(10L)
                                .country("FR")
                                .city("Paris")
                                .stayingDays(5L)
                                .lastModifiedAt(now.plusHours(1)) // more recent than previous budget
                                .build()))
                .build();

        BudgetEstimation oldBudget = BudgetEstimation.builder()
                .id(100L)
                .travelPlanId(travelPlanId)
                .lastModifiedAt(now.plusHours(1)) // still fresh
                .breakdowns(new ArrayList<>(List.of(
                        BudgetEstimationBreakdown.builder()
                                .travelPlanDestinationId(10L)
                                .createdAt(now)
                                .lastModifiedAt(now)
                                .build())))
                .build();

        UserProfileDTO userProfile = UserProfileDTO.builder()
                .id(userId)
                .budgetLevel(BudgetLevel.MEDIUM)
                .travelStyle("Backpacker")
                .build();

        when(travelPlanService.findById(travelPlanId)).thenReturn(travelPlan);
        when(userProfileService.findById(userId)).thenReturn(userProfile);
        when(budgetEstimationRepository.findByTravelPlanId(travelPlanId)).thenReturn(Optional.of(oldBudget));

        InvalidOperationException invalidOperationException = assertThrows(InvalidOperationException.class,
                () -> budgetEstimationService.estimateBudget(userId, travelPlanId));
        assertEquals("There is a fresh BudgetEstimation already done for this travel plan " + travelPlanId, invalidOperationException.getMessage());

        verify(chatGptService, never()).ask(any());
        verify(budgetEstimationRepository, never()).save(any());
    }

}
