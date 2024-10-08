package com.ai.demo.finance.event.retirement;

import com.ai.demo.finance.exception.InvalidOperationException;
import com.ai.demo.finance.model.Account;
import com.ai.demo.finance.model.RetirementDetail;
import com.ai.demo.finance.model.cache.RetirementGoal;
import com.ai.demo.finance.model.repository.AccountRepository;
import com.ai.demo.finance.service.RetirementGoalService;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class RetirementGoalCalculator {

    private static final int DIVISION_SCALE = 4;
    private static final int PERCENTAGE_SCALE = 2;
    private final RetirementGoalService retirementGoalService;
    private final AccountRepository accountRepository;

    /**
     * Calculates the progress towards achieving the retirement goal based on the
     * provided retirement details and list of accounts.
     * @param retirementDetail the retirement details including income per month
     *            desired, retirement date, and life expectation
     * @return the percentage of progress made towards the total savings needed for
     *         retirement
     */
    public RetirementGoal calculateRetirementGoal(@NotNull RetirementDetail retirementDetail) {
        List<Account> accounts = accountRepository.findAllByUserId(retirementDetail.getUserId());

        if (accounts == null || accounts.isEmpty()) {
            throw new InvalidOperationException("Insufficient retirement information to calculate goal.");
        }

        int retirementDurationInMonths = calculateRetirementDurationInMonths(retirementDetail);

        BigDecimal totalSavingNeededToRetire = calculateTotalSavingsNeededToRetire(retirementDetail, retirementDurationInMonths);
        BigDecimal totalSavingGross = calculateTotalNetSavings(accounts);
        BigDecimal percentageToAchieveTheGoal = calculatePercentageFromAchievingRetirement(totalSavingGross, totalSavingNeededToRetire);

        RetirementGoal retirementGoal = new RetirementGoal(retirementDetail.getUserId(), percentageToAchieveTheGoal);
        retirementGoalService.saveRetirementGoal(retirementGoal);

        return retirementGoal;
    }

    /**
     * Formula: totalSavingSoFar / totalSavingNeededToRetire * 100
     * @param totalSavingSoFar
     * @param totalSavingNeededToRetire
     * @return percentage
     */
    private static BigDecimal calculatePercentageFromAchievingRetirement(BigDecimal totalSavingSoFar,
            BigDecimal totalSavingNeededToRetire) {
        return totalSavingSoFar.divide(totalSavingNeededToRetire, DIVISION_SCALE, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).setScale(PERCENTAGE_SCALE, RoundingMode.HALF_UP);
    }

    private static BigDecimal calculateTotalNetSavings(List<Account> accounts) {
        return accounts.stream().map(Account::getAmountNet).filter(Objects::nonNull).reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }

    private static BigDecimal calculateTotalSavingsNeededToRetire(RetirementDetail retirementDetail, int retirementDurationInMonths) {
        BigDecimal incomePerMonthDesired = retirementDetail.getIncomePerMonthDesired();

        return incomePerMonthDesired
                .multiply(BigDecimal.valueOf(retirementDurationInMonths));
    }

    private static int calculateRetirementDurationInMonths(RetirementDetail retirementDetail) {
        LocalDate retirementDate = retirementDetail.getRetirementDate();
        LocalDate lifeExpectation = retirementDetail.getLifeExpectation();

        Period retirementDuration = Period.between(retirementDate, lifeExpectation);
        int retirementDurationInMonths = retirementDuration.getYears() * 12 + retirementDuration.getMonths();

        if (retirementDurationInMonths <= 0) {
            throw new IllegalArgumentException("Retirement duration must be greater than 0 months");
        }
        return retirementDurationInMonths;
    }
}
