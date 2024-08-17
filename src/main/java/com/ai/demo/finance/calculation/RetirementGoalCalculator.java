package com.ai.demo.finance.calculation;

import com.ai.demo.finance.model.Account;
import com.ai.demo.finance.model.RetirementDetail;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class RetirementGoalCalculator {

    private static final int DIVISION_SCALE = 4;
    private static final int PERCENTAGE_SCALE = 2;

    /**
     * Calculates the progress towards achieving the retirement goal based on the
     * provided retirement details and list of accounts.
     * @param retirementDetail the retirement details including income per month
     *            desired, retirement date, and life expectation
     * @param accounts the list of accounts containing savings information
     * @return the percentage of progress made towards the total savings needed for
     *         retirement
     */
    public BigDecimal calculateRetirementGoal(RetirementDetail retirementDetail, List<Account> accounts) {
        int retirementDurationInMonths = calculateRetirementDurationInMonths(retirementDetail);

        BigDecimal totalSavingNeededToRetire = calculateTotalSavingsNeededToRetire(retirementDetail, retirementDurationInMonths);
        BigDecimal totalSavingSoFar = calculateTotalSavingsSoFar(accounts);

        return calculatePercentageFromAchievingRetirement(totalSavingSoFar, totalSavingNeededToRetire);
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

    private static BigDecimal calculateTotalSavingsSoFar(List<Account> accounts) {
        return accounts.stream().map(Account::getAmount).filter(Objects::nonNull).reduce(BigDecimal::add)
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
