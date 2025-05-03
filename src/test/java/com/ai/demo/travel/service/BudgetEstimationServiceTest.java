package com.ai.demo.travel.service;

import static com.ai.demo.travel.helper.UserProfileHelper.defaultUserProfile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.ai.demo.config.ObjectMapperConfig;
import com.ai.demo.finance.ai.ChatGptService;
import com.ai.demo.finance.exception.AICommunicationException;
import com.ai.demo.finance.exception.AIParsingException;
import com.ai.demo.finance.exception.InvalidOperationException;
import com.ai.demo.travel.dto.BudgetBreakdownCostDTO;
import com.ai.demo.travel.dto.BudgetEstimationBreakdownDTO;
import com.ai.demo.travel.dto.TravelPlanDTO;
import com.ai.demo.travel.dto.TravelPlanDestinationDTO;
import com.ai.demo.travel.dto.UserProfileDTO;
import com.ai.demo.travel.helper.TravelPlanHelper;
import com.ai.demo.travel.model.BudgetBreakdownCost;
import com.ai.demo.travel.model.BudgetEstimation;
import com.ai.demo.travel.model.BudgetEstimationBreakdown;
import com.ai.demo.travel.model.repository.BudgetEstimationRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    private BudgetEstimationService budgetEstimationService;

    private final String aiAnswer = "{\n" + "  \"costs\": [\n" + "    {\n" + "      \"description\": \"Flight\",\n"
            + "      \"cost\": 800.00,\n" + "      \"notes\": \"Round trip from Spain to Brazil (Florianopolis)\"\n"
            + "    },\n" + "    {\n" + "      \"description\": \"Accommodation\",\n" + "      \"cost\": 60.00,\n"
            + "      \"notes\": \"Budget guesthouse stay at €60/night\"\n" + "    },\n" + "    {\n"
            + "      \"description\": \"Daily food\",\n" + "      \"cost\": 25.00,\n"
            + "      \"notes\": \"Estimated cost for meals and drinks\"\n" + "    },\n" + "    {\n"
            + "      \"description\": \"Local transportation\",\n" + "      \"cost\": 10.00,\n"
            + "      \"notes\": \"Cost for taxis and buses\"\n" + "    },\n" + "    {\n"
            + "      \"description\": \"Entertainment and activities\",\n" + "      \"cost\": 50.00,\n"
            + "      \"notes\": \"Optional tours and entrance fees\"\n" + "    }\n" + "  ],\n"
            + "  \"total_estimated\": 2425.00\n" + "}";

    @BeforeEach
    public void setUp() {
        this.budgetEstimationService = new BudgetEstimationService(userProfileService, travelPlanService, chatGptService, budgetEstimationRepository,
                new ObjectMapperConfig().objectMapper());
    }

    @Test
    void estimateBudget_shouldCreateNewBudgetEstimation_whenNoneExists() {
        Long userId = 1L;
        Long travelPlanId = 2L;

        var travelPlan = TravelPlanHelper.dtoWithSingleDestination(userId, travelPlanId, 10L);

        UserProfileDTO userProfile = defaultUserProfile(userId);

        when(travelPlanService.findById(travelPlanId)).thenReturn(travelPlan);
        when(userProfileService.findById(userId)).thenReturn(userProfile);
        when(budgetEstimationRepository.findByTravelPlanId(travelPlanId)).thenReturn(Optional.empty());
        when(chatGptService.ask(any())).thenReturn(aiAnswer);
        when(budgetEstimationRepository.save(any(BudgetEstimation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = budgetEstimationService.estimateBudget(travelPlanId);

        assertNotNull(result);
        assertEquals("2425.00", result.getTotalEstimation().toString());
        assertEquals(1, result.getBreakdowns().size());
        assertEquals("2425.00", result.getBreakdowns().get(0).getEstimation().toString());
        assertEquals(5, result.getBreakdowns().get(0).getCosts().size());

        Assertions.assertThat(result.getBreakdowns().get(0).getCosts().stream().allMatch(c -> c.getEstimation() != null)).isTrue();
        Assertions.assertThat(result.getBreakdowns().get(0).getCosts().stream().allMatch(c -> c.getNotes() != null)).isTrue();
        Assertions.assertThat(result.getBreakdowns().get(0).getCosts().stream().map(BudgetBreakdownCostDTO::getDescription).toList())
                .containsExactlyInAnyOrder("Flight", "Accommodation", "Daily food", "Local transportation", "Entertainment and activities");

        verify(budgetEstimationRepository, times(1)).save(any(BudgetEstimation.class));
    }

    @Test
    void estimateBudget_shouldThrowException_whenTravelPlanStartDateIsMissing() {
        Long userId = 1L;
        Long travelPlanId = 2L;

        var incompleteTravelPlan = TravelPlanHelper.dtoWithSingleDestination(userId, travelPlanId, 10L);
        incompleteTravelPlan.setStartDate(null);

        when(travelPlanService.findById(travelPlanId)).thenReturn(incompleteTravelPlan);

        InvalidOperationException ex = assertThrows(InvalidOperationException.class,
                () -> budgetEstimationService.estimateBudget(travelPlanId));

        assertEquals("Travel plan is not completed. Dates are required.", ex.getMessage());
        verifyNoInteractions(userProfileService);
        verifyNoInteractions(budgetEstimationRepository);
    }

    @Test
    void estimateBudget_shouldThrowException_whenTravelPlanEndDateIsMissing() {
        Long userId = 1L;
        Long travelPlanId = 2L;

        var incompleteTravelPlan = TravelPlanHelper.dtoWithSingleDestination(userId, travelPlanId, 10L);
        incompleteTravelPlan.setEndDate(null);

        when(travelPlanService.findById(travelPlanId)).thenReturn(incompleteTravelPlan);

        InvalidOperationException ex = assertThrows(InvalidOperationException.class,
                () -> budgetEstimationService.estimateBudget(travelPlanId));

        assertEquals("Travel plan is not completed. Dates are required.", ex.getMessage());
        verifyNoInteractions(userProfileService);
        verifyNoInteractions(budgetEstimationRepository);
    }

    @Test
    void estimateBudget_shouldReuseExistingBudget_whenNoDestinationChanges() {
        Long userId = 1L;
        Long travelPlanId = 2L;
        LocalDateTime now = LocalDateTime.now();

        var travelPlan = TravelPlanHelper.dtoWithSingleDestination(userId, travelPlanId, 10L);

        BudgetEstimation existingBudget = BudgetEstimation.builder()
                .id(100L)
                .travelPlanId(travelPlanId)
                .lastModifiedAt(now.plusMinutes(-1))
                .totalEstimation(BigDecimal.TEN)
                .breakdowns(new ArrayList<>(List.of(
                        BudgetEstimationBreakdown.builder()
                                .travelPlanDestinationId(10L)
                                .createdAt(now)
                                .lastModifiedAt(now.plusHours(1))
                                .build())))
                .build();

        when(travelPlanService.findById(travelPlanId)).thenReturn(travelPlan);
        when(budgetEstimationRepository.findByTravelPlanId(travelPlanId)).thenReturn(Optional.of(existingBudget));

        var result = budgetEstimationService.estimateBudget(travelPlanId);

        assertNotNull(result);
        assertEquals("10", result.getTotalEstimation().toString());
        assertEquals(existingBudget.getId(), result.getId());
        verify(budgetEstimationRepository, never()).save(any());
        verifyNoInteractions(chatGptService); // No AI call needed
    }

    @Test
    void estimateBudget_shouldReuseExistingBudget_whenNewDestinationAdded() {
        Long userId = 1L;
        Long travelPlanId = 2L;
        LocalDateTime now = LocalDateTime.now();

        var travelPlan = TravelPlanHelper.dtoWithSingleDestination(userId, travelPlanId, 10L);
        travelPlan.getDestinations().get(0).setLastModifiedAt(now.minusHours(1));
        travelPlan.getDestinations().add(TravelPlanDestinationDTO.builder()
                .id(938723L)
                .country("CA")
                .city("NEW ONE")
                .startDate(LocalDate.now().plusDays(5))
                .endDate(LocalDate.now().plusDays(12))
                .lastModifiedAt(now)
                .build());

        BudgetEstimation existingBudget = BudgetEstimation.builder()
                .id(100L)
                .travelPlanId(travelPlanId)
                .lastModifiedAt(now.plusMinutes(-1))
                .breakdowns(new ArrayList<>(List.of(
                        BudgetEstimationBreakdown.builder()
                                .travelPlanDestinationId(10L)
                                .createdAt(now)
                                .lastModifiedAt(now)
                                .estimation(BigDecimal.TEN)
                                .costs(new ArrayList<>(List.of(BudgetBreakdownCost.builder()
                                        .description("Test")
                                        .estimation(BigDecimal.TEN)
                                        .notes("Test2")
                                        .build())))
                                .build())))
                .build();

        UserProfileDTO userProfile = defaultUserProfile(userId);

        when(userProfileService.findById(userId)).thenReturn(userProfile);
        when(travelPlanService.findById(travelPlanId)).thenReturn(travelPlan);
        when(budgetEstimationRepository.findByTravelPlanId(travelPlanId)).thenReturn(Optional.of(existingBudget));
        when(chatGptService.ask(any())).thenReturn(aiAnswer);
        when(budgetEstimationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = budgetEstimationService.estimateBudget(travelPlanId);

        assertNotNull(result);
        assertEquals("2435.00", result.getTotalEstimation().toString());
        assertEquals(2, result.getBreakdowns().size());

        BudgetEstimationBreakdownDTO newAdded = result.getBreakdowns().get(1);
        assertEquals(5, newAdded.getCosts().size());

        Assertions.assertThat(newAdded.getCosts().stream().allMatch(c -> c.getEstimation() != null)).isTrue();
        Assertions.assertThat(newAdded.getCosts().stream().allMatch(c -> c.getNotes() != null)).isTrue();
        Assertions.assertThat(
                newAdded.getCosts().stream().map(BudgetBreakdownCostDTO::getDescription).toList())
                .containsExactlyInAnyOrder("Flight", "Accommodation", "Daily food", "Local transportation", "Entertainment and activities");

        verify(chatGptService, times(1)).ask(any());
        verify(budgetEstimationRepository, times(1)).save(any());

    }

    @Test
    void estimateBudget_shouldUpdateBudget_whenDestinationsModified() {
        Long userId = 1L;
        Long travelPlanId = 2L;
        LocalDateTime now = LocalDateTime.now();

        TravelPlanDTO travelPlan = TravelPlanHelper.dtoWithSingleDestination(userId, travelPlanId, 10L);
        travelPlan.getDestinations().get(0).setLastModifiedAt(now.plusHours(1));
        travelPlan.setLastModifiedAt(now.plusHours(2));

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

        UserProfileDTO userProfile = defaultUserProfile(userId);

        when(travelPlanService.findById(travelPlanId)).thenReturn(travelPlan);
        when(userProfileService.findById(userId)).thenReturn(userProfile);
        when(budgetEstimationRepository.findByTravelPlanId(travelPlanId)).thenReturn(Optional.of(oldBudget));
        when(chatGptService.ask(any())).thenReturn(aiAnswer);
        when(budgetEstimationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = budgetEstimationService.estimateBudget(travelPlanId);

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

        TravelPlanDTO travelPlan = TravelPlanHelper.dtoWithSingleDestination(userId, travelPlanId, 1L);
        travelPlan.getDestinations().get(0).setLastModifiedAt(now.plusHours(1));
        travelPlan.setLastModifiedAt(now);

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

        when(travelPlanService.findById(travelPlanId)).thenReturn(travelPlan);
        when(budgetEstimationRepository.findByTravelPlanId(travelPlanId)).thenReturn(Optional.of(oldBudget));

        InvalidOperationException invalidOperationException = assertThrows(InvalidOperationException.class,
                () -> budgetEstimationService.estimateBudget(travelPlanId));
        assertEquals("There is a fresh BudgetEstimation already done for this travel plan " + travelPlanId, invalidOperationException.getMessage());

        verify(chatGptService, never()).ask(any());
        verify(budgetEstimationRepository, never()).save(any());
    }

    @Test
    void estimateBudget_shouldThrowUponInvalidOutputFromAI() {
        Long userId = 1L;
        Long travelPlanId = 2L;

        TravelPlanDTO travelPlan = TravelPlanHelper.dtoWithSingleDestination(userId, travelPlanId, 1L);

        UserProfileDTO userProfile = defaultUserProfile(userId);

        when(travelPlanService.findById(travelPlanId)).thenReturn(travelPlan);
        when(userProfileService.findById(userId)).thenReturn(userProfile);
        when(budgetEstimationRepository.findByTravelPlanId(travelPlanId)).thenReturn(Optional.empty());
        when(chatGptService.ask(any())).thenReturn("Hallucination from AI");

        Assertions.assertThatThrownBy(() -> budgetEstimationService.estimateBudget(travelPlanId))
                .isInstanceOf(AIParsingException.class);

        verify(budgetEstimationRepository, never()).save(any(BudgetEstimation.class));
    }

    @Test
    void estimateBudget_shouldThrowUponAnyErrorFromAI() {
        Long userId = 1L;
        Long travelPlanId = 2L;

        TravelPlanDTO travelPlan = TravelPlanHelper.dtoWithSingleDestination(userId, travelPlanId, 1L);

        UserProfileDTO userProfile = defaultUserProfile(userId);

        when(travelPlanService.findById(travelPlanId)).thenReturn(travelPlan);
        when(userProfileService.findById(userId)).thenReturn(userProfile);
        when(budgetEstimationRepository.findByTravelPlanId(travelPlanId)).thenReturn(Optional.empty());
        when(chatGptService.ask(any())).thenThrow(new RuntimeException("Any error from AI"));

        Assertions.assertThatThrownBy(() -> budgetEstimationService.estimateBudget(travelPlanId))
                .isInstanceOf(AICommunicationException.class);

        verify(budgetEstimationRepository, never()).save(any(BudgetEstimation.class));
    }
}
