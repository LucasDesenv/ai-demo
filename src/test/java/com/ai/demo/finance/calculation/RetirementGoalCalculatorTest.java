package com.ai.demo.finance.calculation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ai.demo.finance.model.Account;
import com.ai.demo.finance.model.RetirementDetail;
import com.ai.demo.finance.model.enums.AccountType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class RetirementGoalCalculatorTest {

    private final RetirementGoalCalculator retirementGoalCalculator = new RetirementGoalCalculator();

    @Test
    void calculates_retirement_goal_percentage_correctly_with_valid_inputs() {
        // 30 years of retirement expected
        LocalDate retirementDate = LocalDate.now().plusYears(50);
        LocalDate lifeExpectation = retirementDate.plusYears(30);

        RetirementDetail retirementDetail = RetirementDetail.builder()
                .incomePerMonthDesired(new BigDecimal("2000"))
                .lifeExpectation(lifeExpectation)
                .retirementDate(retirementDate)
                .userId(1L)
                .build();

        List<Account> accounts = List.of(
                Account.builder().amount(new BigDecimal("50000")).userId(1L).build(),
                Account.builder().amount(new BigDecimal("100000")).userId(1L).build());

        BigDecimal result = retirementGoalCalculator.calculateRetirementGoal(retirementDetail, accounts);

        assertEquals(new BigDecimal("20.83"), result);
    }

    @Test
    void handles_empty_accounts_list_correctly() {
        RetirementDetail retirementDetail = RetirementDetail.builder()
                .incomePerMonthDesired(new BigDecimal("2000"))
                .lifeExpectation(LocalDate.now().plusYears(50))
                .retirementDate(LocalDate.of(2025, 1, 1))
                .userId(1L)
                .build();

        List<Account> accounts = List.of();

        BigDecimal result = retirementGoalCalculator.calculateRetirementGoal(retirementDetail, accounts);

        assertEquals(BigDecimal.ZERO.setScale(2), result);
    }

    @Test
    void handles_retirement_duration_of_one_month() {
        // Arrange
        RetirementDetail retirementDetail = RetirementDetail.builder()
                .incomePerMonthDesired(new BigDecimal("2000"))
                .lifeExpectation(LocalDate.of(2040, 2, 1))
                .retirementDate(LocalDate.of(2040, 1, 1))
                .userId(1L)
                .build();

        Account account = Account.builder()
                .amount(new BigDecimal("2000"))
                .type(AccountType.SAVINGS)
                .date(LocalDateTime.now())
                .userId(1L)
                .build();

        List<Account> accounts = List.of(account);

        // Act
        BigDecimal result = retirementGoalCalculator.calculateRetirementGoal(retirementDetail, accounts);

        // Assert
        assertEquals(new BigDecimal("100.00"), result);
    }

    @Test
    void handles_retirement_duration_one_year() {
        // Arrange
        RetirementDetail retirementDetail = RetirementDetail.builder()
                .incomePerMonthDesired(new BigDecimal("2000"))
                .lifeExpectation(LocalDate.of(2050, 1, 1))
                .retirementDate(LocalDate.of(2049, 1, 1))
                .userId(1L)
                .build();

        Account account1 = Account.builder()
                .amount(new BigDecimal("5000"))
                .type(AccountType.SAVINGS)
                .date(LocalDateTime.now())
                .userId(1L)
                .build();

        Account account2 = Account.builder()
                .amount(new BigDecimal("3000"))
                .type(AccountType.SAVINGS)
                .date(LocalDateTime.now())
                .userId(1L)
                .build();

        List<Account> accounts = List.of(account1, account2);

        // Act
        BigDecimal result = retirementGoalCalculator.calculateRetirementGoal(retirementDetail, accounts);

        // Assert
        assertEquals(new BigDecimal("33.33"), result);
    }

    @Test
    void handles_zero_savings_so_far() {
        RetirementGoalCalculator calculator = new RetirementGoalCalculator();

        RetirementDetail retirementDetail = RetirementDetail.builder()
                .incomePerMonthDesired(new BigDecimal("2000"))
                .lifeExpectation(LocalDate.of(2050, 1, 1))
                .retirementDate(LocalDate.of(2040, 1, 1))
                .build();

        List<Account> accounts = List.of();

        BigDecimal result = calculator.calculateRetirementGoal(retirementDetail, accounts);

        assertEquals(new BigDecimal("0.00"), result);
    }

    @Test
    void handles_retirement_date_equal_to_life_expectation_date() {
        RetirementGoalCalculator calculator = new RetirementGoalCalculator();

        RetirementDetail retirementDetail = RetirementDetail.builder()
                .incomePerMonthDesired(new BigDecimal("3000"))
                .lifeExpectation(LocalDate.of(2050, 1, 1))
                .retirementDate(LocalDate.of(2050, 1, 1))
                .build();

        List<Account> accounts = List.of(
                Account.builder().amount(new BigDecimal("60000")).build(),
                Account.builder().amount(new BigDecimal("40000")).build());

        Assertions.assertThatThrownBy(() -> calculator.calculateRetirementGoal(retirementDetail, accounts))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Retirement duration must be greater than 0 months");
    }

    @Test
    void handles_null_account_amounts() {
        RetirementGoalCalculator calculator = new RetirementGoalCalculator();

        RetirementDetail retirementDetail = RetirementDetail.builder()
                .incomePerMonthDesired(new BigDecimal("2000"))
                .lifeExpectation(LocalDate.of(2050, 1, 1))
                .retirementDate(LocalDate.of(2040, 1, 1))
                .userId(1L)
                .build();

        List<Account> accounts = List.of(
                Account.builder().amount(new BigDecimal("50000")).userId(1L).build(),
                Account.builder().amount(null).userId(1L).build());

        BigDecimal result = calculator.calculateRetirementGoal(retirementDetail, accounts);

        assertEquals(new BigDecimal("20.83"), result);
    }

    @Test
    void handles_large_values_for_savings_and_income() {
        RetirementGoalCalculator calculator = new RetirementGoalCalculator();

        RetirementDetail retirementDetail = RetirementDetail.builder()
                .incomePerMonthDesired(new BigDecimal("999999999999999999999999999999999999999999999999999999999999999"))
                .lifeExpectation(LocalDate.of(2100, 1, 1))
                .retirementDate(LocalDate.of(2090, 1, 1))
                .userId(1L)
                .build();

        List<Account> accounts = List.of(
                Account.builder().amount(new BigDecimal("99999999999999999999999999999999999999999999999999999999999999")).userId(1L).build(),
                Account.builder().amount(new BigDecimal("88888888888888888888888888888888888888888888888888888888888888")).userId(1L).build());

        BigDecimal result = calculator.calculateRetirementGoal(retirementDetail, accounts);

        assertEquals(new BigDecimal("0.16"), result);
    }
}
