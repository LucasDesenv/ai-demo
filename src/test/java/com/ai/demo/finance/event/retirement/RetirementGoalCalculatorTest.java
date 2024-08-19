package com.ai.demo.finance.event.retirement;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ai.demo.finance.exception.InvalidOperationException;
import com.ai.demo.finance.model.Account;
import com.ai.demo.finance.model.RetirementDetail;
import com.ai.demo.finance.model.cache.RetirementGoal;
import com.ai.demo.finance.model.enums.AccountType;
import com.ai.demo.finance.service.RetirementGoalService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RetirementGoalCalculatorTest {

    @Mock
    private RetirementGoalService retirementGoalService;
    @InjectMocks
    private RetirementGoalCalculator retirementGoalCalculator;

    @Test
    void calculates_retirement_goal_percentage_correctly_with_valid_inputs() {
        // 30 years of retirement goalExpected
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

        BigDecimal result = retirementGoalCalculator.calculateRetirementGoal(retirementDetail, accounts).getGoalPercentage();

        BigDecimal goalExpected = new BigDecimal("20.83");
        assertEquals(goalExpected, result);
        Mockito.verify(retirementGoalService).saveRetirementGoal(new RetirementGoal(1L, goalExpected));
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
        BigDecimal result = retirementGoalCalculator.calculateRetirementGoal(retirementDetail, accounts).getGoalPercentage();

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
        BigDecimal result = retirementGoalCalculator.calculateRetirementGoal(retirementDetail, accounts).getGoalPercentage();

        // Assert
        assertEquals(new BigDecimal("33.33"), result);
    }

    @Test
    void handles_retirement_date_equal_to_life_expectation_date() {

        RetirementDetail retirementDetail = RetirementDetail.builder()
                .incomePerMonthDesired(new BigDecimal("3000"))
                .lifeExpectation(LocalDate.of(2050, 1, 1))
                .retirementDate(LocalDate.of(2050, 1, 1))
                .build();

        List<Account> accounts = List.of(
                Account.builder().amount(new BigDecimal("60000")).build(),
                Account.builder().amount(new BigDecimal("40000")).build());

        Assertions.assertThatThrownBy(() -> retirementGoalCalculator.calculateRetirementGoal(retirementDetail, accounts))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Retirement duration must be greater than 0 months");
    }

    @Test
    void handles_null_account_amounts() {

        RetirementDetail retirementDetail = RetirementDetail.builder()
                .incomePerMonthDesired(new BigDecimal("2000"))
                .lifeExpectation(LocalDate.of(2050, 1, 1))
                .retirementDate(LocalDate.of(2040, 1, 1))
                .userId(1L)
                .build();

        List<Account> accounts = List.of(
                Account.builder().amount(new BigDecimal("50000")).userId(1L).build(),
                Account.builder().amount(null).userId(1L).build());

        BigDecimal result = retirementGoalCalculator.calculateRetirementGoal(retirementDetail, accounts).getGoalPercentage();

        assertEquals(new BigDecimal("20.83"), result);
    }

    @Test
    void handles_large_values_for_savings_and_income() {

        RetirementDetail retirementDetail = RetirementDetail.builder()
                .incomePerMonthDesired(new BigDecimal("999999999999999999999999999999999999999999999999999999999999999"))
                .lifeExpectation(LocalDate.of(2100, 1, 1))
                .retirementDate(LocalDate.of(2090, 1, 1))
                .userId(1L)
                .build();

        List<Account> accounts = List.of(
                Account.builder().amount(new BigDecimal("99999999999999999999999999999999999999999999999999999999999999")).userId(1L).build(),
                Account.builder().amount(new BigDecimal("88888888888888888888888888888888888888888888888888888888888888")).userId(1L).build());

        BigDecimal result = retirementGoalCalculator.calculateRetirementGoal(retirementDetail, accounts).getGoalPercentage();

        assertEquals(new BigDecimal("0.16"), result);
    }

    @Test
    void test_handles_empty_account_list() {

        RetirementDetail retirementDetail = RetirementDetail.builder()
                .incomePerMonthDesired(new BigDecimal("2000"))
                .retirementDate(LocalDate.of(2025, 1, 1))
                .lifeExpectation(LocalDate.of(2045, 1, 1))
                .userId(1L)
                .build();

        List<Account> accounts = List.of();

        Assertions.assertThatThrownBy(() -> retirementGoalCalculator.calculateRetirementGoal(retirementDetail, accounts))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessage("Insufficient retirement information to calculate goal.");
    }

    @Test
    void test_handles_null_account_amounts() {

        RetirementDetail retirementDetail = RetirementDetail.builder()
                .incomePerMonthDesired(new BigDecimal("3000"))
                .retirementDate(LocalDate.of(2030, 1, 1))
                .lifeExpectation(LocalDate.of(2050, 1, 1))
                .userId(2L)
                .build();

        List<Account> accounts = null;

        Assertions.assertThatThrownBy(() -> retirementGoalCalculator.calculateRetirementGoal(retirementDetail, accounts))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessage("Insufficient retirement information to calculate goal.");
    }

    @Test
    void test_handle_null_retirement_detail() {

        List<Account> accounts = List.of(
                Account.builder().amount(new BigDecimal("50000")).userId(1L).build(),
                Account.builder().amount(new BigDecimal("100000")).userId(1L).build());

        Assertions.assertThatThrownBy(() -> retirementGoalCalculator.calculateRetirementGoal(null, accounts))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessage("Insufficient retirement information to calculate goal.");
    }
}
