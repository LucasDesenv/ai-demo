package com.ai.demo.travel.service;

import static com.ai.demo.utils.CountryCodesUtil.ISO_COUNTRIES;

import com.ai.demo.finance.ai.ChatGptService;
import com.ai.demo.finance.ai.PromptLoader;
import com.ai.demo.finance.exception.AICommunicationException;
import com.ai.demo.finance.exception.AIParsingException;
import com.ai.demo.finance.exception.InvalidOperationException;
import com.ai.demo.finance.exception.NotFoundResourceException;
import com.ai.demo.travel.dto.AIBudgetResponse;
import com.ai.demo.travel.dto.BudgetEstimationDTO;
import com.ai.demo.travel.dto.TravelPlanDTO;
import com.ai.demo.travel.dto.TravelPlanDestinationDTO;
import com.ai.demo.travel.dto.UserProfileDTO;
import com.ai.demo.travel.mapper.BudgetEstimationMapper;
import com.ai.demo.travel.model.BudgetEstimation;
import com.ai.demo.travel.model.BudgetEstimationBreakdown;
import com.ai.demo.travel.model.TravelerType;
import com.ai.demo.travel.model.repository.BudgetEstimationRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BudgetEstimationService {

    private static final BudgetEstimationMapper MAPPER = Mappers.getMapper(BudgetEstimationMapper.class);
    private final UserProfileService userProfileService;
    private final TravelPlanService travelPlanService;
    private final ChatGptService chatGptService;
    private final BudgetEstimationRepository budgetEstimationRepository;
    private final ObjectMapper objectMapper;

    private record DestinationsDetected(Map<Long, BudgetEstimationBreakdown> breakdownsPerDestination,
            List<TravelPlanDestinationDTO> destinationsToUpdate, List<TravelPlanDestinationDTO> freshDestinations) {
    }

    @Transactional
    public BudgetEstimationDTO estimateBudget(Long travelPlanId) {
        var travelPlan = travelPlanService.findById(travelPlanId);
        validateTravelPlanIsReady(travelPlan);

        var optionalBudgetEstimation = budgetEstimationRepository.findByTravelPlanId(travelPlanId);

        if (optionalBudgetEstimation.isPresent()) {
            var updatedBudgetEstimation = updateBudgetEstimation(travelPlanId, optionalBudgetEstimation.get(), travelPlan);
            return MAPPER.toDTO(updatedBudgetEstimation);
        }

        var newBudgetEstimation = createBudgetEstimation(travelPlanId, travelPlan);

        return MAPPER.toDTO(newBudgetEstimation);
    }

    private BudgetEstimation createBudgetEstimation(Long travelPlanId, TravelPlanDTO travelPlan) {
        var userProfile = userProfileService.findById(travelPlan.getUserProfileId());

        var breakdowns = createBudgetEstimationBreakDownsFromAI(userProfile, travelPlan, travelPlan.getDestinations());
        var budgetEstimation = BudgetEstimation.builder()
                .travelPlanId(travelPlanId)
                .breakdowns(breakdowns)
                .build();
        budgetEstimation.prepareForCreation();
        return budgetEstimationRepository.save(budgetEstimation);
    }

    private BudgetEstimation updateBudgetEstimation(Long travelPlanId, BudgetEstimation existingBudget, TravelPlanDTO travelPlan) {
        validateIfBudgetEstimationCanBeUpdated(travelPlanId, existingBudget, travelPlan);

        DestinationsDetected result = detectModifiedOrNewDestinations(existingBudget, travelPlan);

        if (result.destinationsToUpdate().isEmpty() && result.freshDestinations().isEmpty()) {
            return existingBudget;
        }

        updateBreakdowns(existingBudget, travelPlan, result);

        return budgetEstimationRepository.save(existingBudget);
    }

    private void updateBreakdowns(BudgetEstimation existingBudget, TravelPlanDTO travelPlan, DestinationsDetected destinationsDetected) {
        UserProfileDTO userProfile = userProfileService.findById(travelPlan.getUserProfileId());

        var newBreakDowns = createBudgetEstimationBreakDownsFromAI(userProfile, travelPlan, destinationsDetected.freshDestinations());

        destinationsDetected.destinationsToUpdate()
                .forEach(toUpdate -> updateBreakdownFromAI(userProfile,
                        travelPlan, toUpdate, destinationsDetected.breakdownsPerDestination().get(toUpdate.getId())));

        existingBudget.addNewCosts(newBreakDowns);
    }

    private static DestinationsDetected detectModifiedOrNewDestinations(BudgetEstimation existingBudget,
            TravelPlanDTO travelPlan) {
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

        return new DestinationsDetected(breakdownsPerDestination, destinationsToUpdate, freshDestinations);
    }

    private static void validateIfBudgetEstimationCanBeUpdated(Long travelPlanId, BudgetEstimation budgetEstimation,
            TravelPlanDTO travelPlan) {
        boolean budgetIsStillFresh = travelPlan.getLastModifiedAt().isBefore(budgetEstimation.getLastModifiedAt());
        if (budgetIsStillFresh) {
            throw new InvalidOperationException(
                    "There is a fresh BudgetEstimation already done for this travel plan %d".formatted(travelPlanId));
        }
    }

    private void updateBreakdownFromAI(UserProfileDTO userProfile,
            TravelPlanDTO travelPlan, TravelPlanDestinationDTO destination, BudgetEstimationBreakdown breakdown) {
        AIBudgetResponse aiBudgetResponse = askAI(userProfile, travelPlan, destination);
        breakdown.replaceCosts(aiBudgetResponse);
    }

    private List<BudgetEstimationBreakdown> createBudgetEstimationBreakDownsFromAI(UserProfileDTO userProfile,
            TravelPlanDTO travelPlan, List<TravelPlanDestinationDTO> destinations) {
        List<BudgetEstimationBreakdown> breakdowns = new ArrayList<>();
        destinations.forEach(destination -> {
            AIBudgetResponse aiBudgetResponse = askAI(userProfile, travelPlan, destination);
            var breakdown = BudgetEstimationBreakdown.builder()
                    .travelPlanDestinationId(destination.getId())
                    .costs(new ArrayList<>())
                    .estimation(aiBudgetResponse.getTotalEstimated())
                    .build();

            breakdown.replaceCosts(aiBudgetResponse);

            breakdowns.add(breakdown);
        });

        return breakdowns;
    }

    private void validateTravelPlanIsReady(TravelPlanDTO travelPlan) {
        if (travelPlan.getStartDate() == null || travelPlan.getEndDate() == null) {
            throw new InvalidOperationException("Travel plan is not completed. Dates are required.");
        }
    }

    private AIBudgetResponse askAI(UserProfileDTO userProfile, TravelPlanDTO travelPlan, TravelPlanDestinationDTO destination) {
        String answer = null;
        try {
            String prompt = buildBudgetPrompt(userProfile, travelPlan, destination);
            answer = chatGptService.ask(prompt);
            return objectMapper.readValue(answer, AIBudgetResponse.class);
        } catch (JsonProcessingException e) {
            throw new AIParsingException("Error while parsing AI budget response. Answer received: %s".formatted(answer), e);
        } catch (Exception e) {
            throw new AICommunicationException("Unexpected error when requesting AI.", e);
        }
    }

    private String buildBudgetPrompt(UserProfileDTO userProfile, TravelPlanDTO travelPlan, TravelPlanDestinationDTO destination) {
        String rawPrompt = PromptLoader.get(PromptLoader.Prompts.BUDGETS);
        String durationInDays = String.valueOf(
                Period.between(destination.getStartDate(), destination.getEndDate()).getDays());
        Map<TravelerType, Long> collect = travelPlan.getTravelerTypes().stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        String travelers = collect.entrySet().stream().map(entry -> entry.getValue() + "x" + entry.getKey().name())
                .collect(Collectors.joining());
        return rawPrompt.replace("{{budgetLevel}}", userProfile.getBudgetLevel().name())
                .replace("{{travelStyle}}", userProfile.getTravelStyle())
                .replace("{{originCountry}}", ISO_COUNTRIES.get(travelPlan.getOriginCountry()))
                .replace("{{destination}}", formatDestination(destination))
                .replace("{{tripType}}", travelPlan.getTripType().name())
                .replace("{{durationInDays}}", durationInDays)
                .replace("{{travelers}}", travelers);
    }

    private String formatDestination(TravelPlanDestinationDTO destination) {
        return "%s (%s)".formatted(ISO_COUNTRIES.get(destination.getCountry()), destination.getCity());
    }

    public BudgetEstimationDTO findByTravelPlanId(Long travelPlanId) {
        return budgetEstimationRepository.findByTravelPlanId(travelPlanId).map(MAPPER::toDTO)
                .orElseThrow(() -> new NotFoundResourceException("Budget estimation not found travel: " + travelPlanId));
    }
}
