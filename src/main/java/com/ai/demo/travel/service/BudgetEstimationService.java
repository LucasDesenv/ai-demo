package com.ai.demo.travel.service;

import static com.ai.demo.utils.CountryCodesUtil.ISO_COUNTRIES;

import com.ai.demo.finance.ai.ChatGptService;
import com.ai.demo.finance.exception.InvalidOperationException;
import com.ai.demo.travel.dto.BudgetEstimationDTO;
import com.ai.demo.travel.dto.TravelPlanDTO;
import com.ai.demo.travel.dto.TravelPlanDestinationDTO;
import com.ai.demo.travel.dto.UserProfileDTO;
import com.ai.demo.travel.mapper.BudgetEstimationMapper;
import com.ai.demo.travel.model.BudgetEstimation;
import com.ai.demo.travel.model.BudgetEstimationBreakdown;
import com.ai.demo.travel.model.repository.BudgetEstimationRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BudgetEstimationService {

    private static final BudgetEstimationMapper MAPPER = Mappers.getMapper(BudgetEstimationMapper.class);
    private final UserProfileService userProfileService;
    private final TravelPlanService travelPlanService;
    private final ChatGptService chatGptService;
    private final BudgetEstimationRepository budgetEstimationRepository;

    public BudgetEstimationDTO estimateBudget(Long userId, Long travelPlanId) {
        TravelPlanDTO travelPlan = travelPlanService.findById(travelPlanId);
        validateTravelPlanIsReady(travelPlan);
        UserProfileDTO userProfile = userProfileService.findById(userId);

        Optional<BudgetEstimation> optionalBudgetEstimation = budgetEstimationRepository.findByTravelPlanId(travelPlanId);

        if (optionalBudgetEstimation.isPresent()) {
            BudgetEstimation updatedBudgetEstimation = updateBudgetEstimation(travelPlanId, optionalBudgetEstimation.get(), travelPlan,
                    userProfile);

            return MAPPER.toDTO(updatedBudgetEstimation);
        }

        BudgetEstimation newBudgetEstimation = createBudgetEstimation(travelPlanId, userProfile, travelPlan);

        return MAPPER.toDTO(newBudgetEstimation);
    }

    private BudgetEstimation createBudgetEstimation(Long travelPlanId, UserProfileDTO userProfile, TravelPlanDTO travelPlan) {
        List<BudgetEstimationBreakdown> breakdowns = createBudgetEstimationBreakDownsFromAI(userProfile, travelPlan, travelPlan.getDestinations());
        BudgetEstimation budgetEstimation = BudgetEstimation.builder()
                .travelPlanId(travelPlanId)
                .breakdowns(breakdowns)
                .build();
        budgetEstimation.prepareForCreation();
        return budgetEstimationRepository.save(budgetEstimation);
    }

    private BudgetEstimation updateBudgetEstimation(Long travelPlanId, BudgetEstimation existingBudget, TravelPlanDTO travelPlan,
            UserProfileDTO userProfile) {
        validateIfBudgetEstimationCanBeUpdated(travelPlanId, existingBudget, travelPlan);

        Map<Long, BudgetEstimationBreakdown> breakdownsPerDestination = existingBudget.getBreakdowns().stream()
                .collect(Collectors.toMap(BudgetEstimationBreakdown::getTravelPlanDestinationId, Function.identity()));

        List<TravelPlanDestinationDTO> destinationsToUpdate = new ArrayList<>();
        List<TravelPlanDestinationDTO> freshDestinations = new ArrayList<>();

        travelPlan.getDestinations().forEach(destination -> {
            if (breakdownsPerDestination.containsKey(destination.getId())) {
                BudgetEstimationBreakdown existingBreakdown = breakdownsPerDestination.get(destination.getId());
                if (destination.getLastModifiedAt().isAfter(existingBreakdown.getLastModifiedAt())) {
                    destinationsToUpdate.add(destination);
                }
            } else {
                freshDestinations.add(destination);
            }
        });

        if (destinationsToUpdate.isEmpty() && freshDestinations.isEmpty()) {
            return existingBudget;
        }

        List<BudgetEstimationBreakdown> newBreakDowns = createBudgetEstimationBreakDownsFromAI(userProfile, travelPlan, freshDestinations);

        destinationsToUpdate.forEach(des -> updateBudgetEstimationsFromAI(userProfile, travelPlan, des, breakdownsPerDestination.get(des.getId())));

        existingBudget.getBreakdowns().addAll(newBreakDowns);

        return budgetEstimationRepository.save(existingBudget);
    }

    private static void validateIfBudgetEstimationCanBeUpdated(Long travelPlanId, BudgetEstimation budgetEstimation,
            TravelPlanDTO travelPlan) {
        boolean budgetIsStillFresh = travelPlan.getLastModifiedAt().isBefore(budgetEstimation.getLastModifiedAt());
        if (budgetIsStillFresh) {
            throw new InvalidOperationException(
                    "There is a fresh BudgetEstimation already done for this travel plan %d".formatted(travelPlanId));
        }
    }

    private void updateBudgetEstimationsFromAI(UserProfileDTO userProfile,
            TravelPlanDTO travelPlan, TravelPlanDestinationDTO destination, BudgetEstimationBreakdown breakdown) {
        String prompt = buildBudgetPrompt(userProfile, travelPlan, destination);
        String answer = chatGptService.ask(prompt);
        breakdown.updateNotes(answer);
    }

    private List<BudgetEstimationBreakdown> createBudgetEstimationBreakDownsFromAI(UserProfileDTO userProfile,
            TravelPlanDTO travelPlan, List<TravelPlanDestinationDTO> destinations) {
        List<BudgetEstimationBreakdown> breakdowns = new ArrayList<>();
        destinations.forEach(destination -> {
            String prompt = buildBudgetPrompt(userProfile, travelPlan, destination);
            String answer = chatGptService.ask(prompt);
            breakdowns.add(BudgetEstimationBreakdown.builder()
                    .travelPlanDestinationId(destination.getId())
                    .notes(answer)
                    .build());
        });

        return breakdowns;
    }

    private void validateTravelPlanIsReady(TravelPlanDTO travelPlan) {
        if (travelPlan.getStartDate() == null || travelPlan.getEndDate() == null) {
            throw new InvalidOperationException("Travel plan is not completed. Dates are required.");
        }
    }

    private String buildBudgetPrompt(UserProfileDTO userProfile, TravelPlanDTO travelPlan, TravelPlanDestinationDTO destination) {
        return String.format("""
                You are a travel budget expert helping a traveler estimate their total trip cost.

                The traveler profile:
                - Travel style: %s
                - Budget level: %s

                The trip details:
                - Country of origin: %s
                - Destinations: %s
                - Trip Type: %s
                - Duration: %d days

                Estimate the following:
                1. Flight cost (approximate)
                2. Accommodation per night (based on budget level and travel style)
                3. Daily food cost
                4. Local transportation cost (e.g., taxis, buses)
                5. Entertainment and activity costs (optional tours, attractions)

                Finally, provide:
                - Total estimated budget = (flight + (accommodation + food + transportation + entertainment) \u00d7 number_of_days)

                Notes:
                - Keep answers concise and practical.
                - Give amounts in EUR (€).
                - Assume solo travel.

                Provide the breakdown and the final estimated total cost.
                """,
                userProfile.getTravelStyle(),
                userProfile.getBudgetLevel(),
                ISO_COUNTRIES.get(travelPlan.getOriginCountry()),
                formatDestination(destination),
                travelPlan.getTripType(),
                destination.getStayingDays());
    }

    private String formatDestination(TravelPlanDestinationDTO destination) {
        return "%s (%s)".formatted(ISO_COUNTRIES.get(destination.getCountry()), destination.getCity());
    }
}
