package com.ai.demo.finance.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.ai.demo.finance.exception.InvalidOperationException;
import com.ai.demo.finance.model.enums.AccountType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class AccountTest {
    // Adding a null balance throws IllegalArgumentException
    @ParameterizedTest
    @ValueSource(strings = {"0", "-1"})
    void test_add_invalid_balance_throws_exception(String balance) {
        Account account = Account.builder()
                .amount(BigDecimal.TEN)
                .build();
        BigDecimal deposit = BigDecimal.valueOf(Long.parseLong(balance));
        assertThrows(InvalidOperationException.class, () -> account.deposit(deposit));
    }

    @Test
    void test_add_null_balance_throws_exception() {
        Account account = Account.builder()
                .amount(BigDecimal.TEN)
                .build();
        assertThrows(InvalidOperationException.class, () -> account.deposit(null));
    }

    // Adding a positive balance updates the account amount correctly
    @Test
    void test_add_positive_balance_updates_amount() {
        Account account = Account.builder()
                .amount(BigDecimal.valueOf(100))
                .build();
        BigDecimal amountToAdd = BigDecimal.valueOf(50);
        AccountHistory history = account.deposit(amountToAdd);
        assertEquals(BigDecimal.valueOf(150), account.getAmount());
        assertNotNull(history);
    }

    // Adding a positive balance creates a new AccountHistory entry
    @Test
    void test_positive_balance_creates_new_history_entry() {
        // Arrange
        Account account = new Account();
        BigDecimal initialAmount = new BigDecimal("100.00");
        Account.builder()
                .amount(initialAmount)
                .id(133L);

        // Act
        AccountHistory history = account.deposit(new BigDecimal("50.00"));

        // Assert
        assertNotNull(history);

        assertEquals(account.getId(), history.getAccountId());
        assertNull(history.getAmount());
        assertNotNull(history.getDate());
    }

    // Adding a positive balance appends to existing history
    @Test
    void test_positive_balance_appends_to_history() {
        // Initialize Account object
        Account account = Account.builder()
                .amount(BigDecimal.valueOf(100))
                .id(1L)
                .type(AccountType.SAVINGS)
                .date(LocalDateTime.now())
                .build();

        // Invoke addBalance method with positive balance
        AccountHistory history = account.deposit(new BigDecimal("50"));

        // Assertions
        assertEquals(new BigDecimal("150"), account.getAmount());
        assertNotNull(history);
    }
}
