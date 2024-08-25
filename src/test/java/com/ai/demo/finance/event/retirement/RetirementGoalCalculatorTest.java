package com.ai.demo.finance.event.retirement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.ai.demo.finance.exception.InvalidOperationException;
import com.ai.demo.finance.model.Account;
import com.ai.demo.finance.model.RetirementDetail;
import com.ai.demo.finance.model.cache.RetirementGoal;
import com.ai.demo.finance.model.enums.AccountType;
import com.ai.demo.finance.model.repository.AccountRepository;
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

    public static final long USER_ID = 1L;
    @Mock
    private RetirementGoalService retirementGoalService;
    @Mock
    private AccountRepository accountRepository;
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
                .userId(USER_ID)
                .build();

        List<Account> accounts = List.of(
                Account.builder().amountNet(new BigDecimal("50000")).userId(USER_ID).build(),
                Account.builder().amountNet(new BigDecimal("100000")).userId(USER_ID).build());
        when(accountRepository.findAllByUserId(USER_ID)).thenReturn(accounts);

        BigDecimal result = retirementGoalCalculator.calculateRetirementGoal(retirementDetail).getGoalPercentage();

        BigDecimal goalExpected = new BigDecimal("20.83");
        assertEquals(goalExpected, result);
        Mockito.verify(retirementGoalService).saveRetirementGoal(new RetirementGoal(USER_ID, goalExpected));
    }

    @Test
    void handles_retirement_duration_of_one_month() {
        // Arrange
        RetirementDetail retirementDetail = RetirementDetail.builder()
                .incomePerMonthDesired(new BigDecimal("2000"))
                .lifeExpectation(LocalDate.of(2040, 2, 1))
                .retirementDate(LocalDate.of(2040, 1, 1))
                .userId(USER_ID)
                .build();

        Account account = Account.builder()
                .amountNet(new BigDecimal("2000"))
                .type(AccountType.SAVINGS)
                .date(LocalDateTime.now())
                .userId(USER_ID)
                .build();

        List<Account> accounts = List.of(account);
        when(accountRepository.findAllByUserId(USER_ID)).thenReturn(accounts);

        // Act
        BigDecimal result = retirementGoalCalculator.calculateRetirementGoal(retirementDetail).getGoalPercentage();

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
                .userId(USER_ID)
                .build();

        Account account1 = Account.builder()
                .amountNet(new BigDecimal("5000"))
                .type(AccountType.SAVINGS)
                .date(LocalDateTime.now())
                .userId(USER_ID)
                .build();

        Account account2 = Account.builder()
                .amountNet(new BigDecimal("3000"))
                .type(AccountType.SAVINGS)
                .date(LocalDateTime.now())
                .userId(USER_ID)
                .build();

        List<Account> accounts = List.of(account1, account2);
        when(accountRepository.findAllByUserId(USER_ID)).thenReturn(accounts);

        // Act
        BigDecimal result = retirementGoalCalculator.calculateRetirementGoal(retirementDetail).getGoalPercentage();

        // Assert
        assertEquals(new BigDecimal("33.33"), result);
    }

    @Test
    void handles_retirement_date_equal_to_life_expectation_date() {

        RetirementDetail retirementDetail = RetirementDetail.builder()
                .incomePerMonthDesired(new BigDecimal("3000"))
                .lifeExpectation(LocalDate.of(2050, 1, 1))
                .retirementDate(LocalDate.of(2050, 1, 1))
                .userId(USER_ID)
                .build();

        List<Account> accounts = List.of(
                Account.builder().amountNet(new BigDecimal("60000")).userId(USER_ID).build(),
                Account.builder().amountNet(new BigDecimal("40000")).userId(USER_ID).build());
        when(accountRepository.findAllByUserId(USER_ID)).thenReturn(accounts);

        Assertions.assertThatThrownBy(() -> retirementGoalCalculator.calculateRetirementGoal(retirementDetail))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Retirement duration must be greater than 0 months");
    }

    @Test
    void handles_null_account_amounts() {

        RetirementDetail retirementDetail = RetirementDetail.builder()
                .incomePerMonthDesired(new BigDecimal("2000"))
                .lifeExpectation(LocalDate.of(2050, 1, 1))
                .retirementDate(LocalDate.of(2040, 1, 1))
                .userId(USER_ID)
                .build();

        List<Account> accounts = List.of(
                Account.builder().amountNet(new BigDecimal("50000")).userId(USER_ID).build(),
                Account.builder().amountNet(null).userId(USER_ID).build());
        when(accountRepository.findAllByUserId(USER_ID)).thenReturn(accounts);

        BigDecimal result = retirementGoalCalculator.calculateRetirementGoal(retirementDetail).getGoalPercentage();

        assertEquals(new BigDecimal("20.83"), result);
    }

    @Test
    void handles_large_values_for_savings_and_income() {

        RetirementDetail retirementDetail = RetirementDetail.builder()
                .incomePerMonthDesired(new BigDecimal("999999999999999999999999999999999999999999999999999999999999999"))
                .lifeExpectation(LocalDate.of(2100, 1, 1))
                .retirementDate(LocalDate.of(2090, 1, 1))
                .userId(USER_ID)
                .build();

        List<Account> accounts = List.of(
                Account.builder().amountNet(new BigDecimal("99999999999999999999999999999999999999999999999999999999999999")).userId(
                        USER_ID).build(),
                Account.builder().amountNet(new BigDecimal("88888888888888888888888888888888888888888888888888888888888888")).userId(
                        USER_ID).build());
        when(accountRepository.findAllByUserId(USER_ID)).thenReturn(accounts);

        BigDecimal result = retirementGoalCalculator.calculateRetirementGoal(retirementDetail).getGoalPercentage();

        assertEquals(new BigDecimal("0.16"), result);
    }

    @Test
    void test_handles_empty_account_list() {

        RetirementDetail retirementDetail = RetirementDetail.builder()
                .incomePerMonthDesired(new BigDecimal("2000"))
                .retirementDate(LocalDate.of(2025, 1, 1))
                .lifeExpectation(LocalDate.of(2045, 1, 1))
                .userId(USER_ID)
                .build();

        List<Account> accounts = List.of();
        when(accountRepository.findAllByUserId(USER_ID)).thenReturn(accounts);

        Assertions.assertThatThrownBy(() -> retirementGoalCalculator.calculateRetirementGoal(retirementDetail))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessage("Insufficient retirement information to calculate goal.");
    }

    @Test
    void test_handles_null_account_amounts() {

        RetirementDetail retirementDetail = RetirementDetail.builder()
                .incomePerMonthDesired(new BigDecimal("3000"))
                .retirementDate(LocalDate.of(2030, 1, 1))
                .lifeExpectation(LocalDate.of(2050, 1, 1))
                .userId(USER_ID)
                .build();

        List<Account> accounts = null;
        when(accountRepository.findAllByUserId(USER_ID)).thenReturn(accounts);

        Assertions.assertThatThrownBy(() -> retirementGoalCalculator.calculateRetirementGoal(retirementDetail))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessage("Insufficient retirement information to calculate goal.");
    }
}
