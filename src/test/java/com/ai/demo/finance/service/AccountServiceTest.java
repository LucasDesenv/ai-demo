package com.ai.demo.finance.service;

import static com.ai.demo.finance.model.enums.AccountType.SAVINGS;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ai.demo.finance.dto.AccountDTO;
import com.ai.demo.finance.dto.BalanceDTO;
import com.ai.demo.finance.dto.UserDTO;
import com.ai.demo.finance.event.EventSource;
import com.ai.demo.finance.event.account.AccountNetAmountPerUserEvent;
import com.ai.demo.finance.event.retirement.RetirementGoalEvent;
import com.ai.demo.finance.exception.NotFoundResourceException;
import com.ai.demo.finance.model.Account;
import com.ai.demo.finance.model.AccountHistory;
import com.ai.demo.finance.model.cache.InflationRate;
import com.ai.demo.finance.model.enums.Country;
import com.ai.demo.finance.model.repository.AccountHistoryRepository;
import com.ai.demo.finance.model.repository.AccountRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private AccountHistoryRepository historyRepository;
    @Mock
    private UserService userService;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private InflationService inflationService;
    @InjectMocks
    private AccountService accountService;

    @Test
    void test_create_account_success() {
        // Arrange
        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        AccountDTO dto = new AccountDTO(1L, "my", new BigDecimal("1000"), SAVINGS, "john");

        when(userService.findByUsername("john")).thenReturn(new UserDTO(2L, "john", Country.BR));
        when(accountRepository.save(accountCaptor.capture())).then(AdditionalAnswers.returnsFirstArg());

        // Act & Assert
        AccountDTO created = accountService.createAccount(dto);
        assertEquals(dto.amount(), created.amount());
        assertEquals(dto.type(), created.type());
        assertNotEquals(dto.id(), created.id());

        Account accountCaptorValue = accountCaptor.getValue();
        assertNull(accountCaptorValue.getId());
        verify(eventPublisher).publishEvent(new AccountNetAmountPerUserEvent(2L, EventSource.ACCOUNT_CREATION));
    }

    @Test
    void test_find_account_not_found() {
        // Arrange
        Long id = 1L;

        when(accountRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundResourceException.class, () -> accountService.findById(id));
    }

    @Test
    void test_finding_existing_account_by_id() {
        // Arrange
        Long id = 1L;
        Account account = Account.builder().id(id).amount(new BigDecimal("1000")).type(SAVINGS).date(LocalDateTime.now()).build();

        when(accountRepository.findById(id)).thenReturn(Optional.of(account));

        // Act & Assert
        AccountDTO found = accountService.findById(id);
        assertEquals(account.getId(), found.id());
    }

    @Test
    void test_update_account_success() {
        // Arrange
        Long id = 1L;
        AccountDTO dto = new AccountDTO(id, "my", new BigDecimal("2000"), SAVINGS, null);

        when(accountRepository.existsById(id)).thenReturn(true);
        when(accountRepository.save(any(Account.class))).then(AdditionalAnswers.returnsFirstArg());

        // Act & Assert
        AccountDTO updated = accountService.updateAccount(id, dto);
        assertEquals(dto, updated);
    }

    @Test
    void test_update_account_not_found() {
        // Arrange
        Long id = 1L;
        AccountDTO dto = new AccountDTO(id, "my", new BigDecimal("2000"), SAVINGS, "john");

        when(accountRepository.existsById(id)).thenReturn(false);

        // Act & Assert
        assertThrows(NotFoundResourceException.class, () -> accountService.updateAccount(id, dto));
    }

    @Test
    void test_delete_account_success() {
        // Arrange
        Long id = 1L;

        when(accountRepository.existsById(id)).thenReturn(true);

        // Act & Assert
        assertDoesNotThrow(() -> accountService.deleteAccount(id));
    }

    @Test
    void test_delete_account_not_found() {
        // Arrange
        Long id = 1L;

        when(accountRepository.existsById(id)).thenReturn(false);

        // Act & Assert
        assertThrows(NotFoundResourceException.class, () -> accountService.deleteAccount(id));
    }

    @Test
    void test_deposit_success() {

        Long accountId = 1L;
        BigDecimal depositAmount = new BigDecimal("100.00");
        BalanceDTO balanceDTO = new BalanceDTO(depositAmount);

        long userId = 39L;
        Account account = new Account(accountId, "my", new BigDecimal("200.00"), null, SAVINGS, LocalDateTime.now(), userId);
        AccountHistory accountHistory = new AccountHistory(account);
        Account updatedAccount = new Account(accountId, "my", new BigDecimal("300.00"), null, SAVINGS, LocalDateTime.now(), userId);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(updatedAccount);
        when(historyRepository.save(any(AccountHistory.class))).thenReturn(accountHistory);

        AccountDTO result = accountService.deposit(accountId, balanceDTO);

        assertNotNull(result);
        assertEquals(updatedAccount.getAmount(), result.amount());
        verify(accountRepository).save(any(Account.class));
        verify(historyRepository).save(any(AccountHistory.class));
        verify(eventPublisher).publishEvent(new AccountNetAmountPerUserEvent(userId, EventSource.DEPOSIT));
    }

    @Test
    void test_deposit_account_not_found() {
        Long accountId = 1L;
        BigDecimal depositAmount = new BigDecimal("100.00");
        BalanceDTO balanceDTO = new BalanceDTO(depositAmount);

        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThrows(NotFoundResourceException.class, () -> accountService.deposit(accountId, balanceDTO));
        verify(accountRepository, never()).save(any(Account.class));
        verify(historyRepository, never()).save(any(AccountHistory.class));
        verify(eventPublisher, never()).publishEvent(any(RetirementGoalEvent.class));
    }

    // Successfully recalculates net amount for all accounts of a user when
    // inflation rate is available
    @Test
    void test_recalculate_net_amount_success() {
        Long userId = 1L;
        UserDTO userDTO = new UserDTO(userId, "testuser", Country.US);
        InflationRate inflationRate = InflationRate.builder().percentageRate(BigDecimal.valueOf(2)).country(Country.US).build();
        Account account1 = Account.builder().id(1L).amount(BigDecimal.valueOf(100)).userId(userId).build();
        Account account2 = Account.builder().id(2L).amount(BigDecimal.valueOf(200)).userId(userId).build();
        List<Account> accounts = Arrays.asList(account1, account2);

        when(userService.findById(userId)).thenReturn(userDTO);
        when(inflationService.fetchLatestMonthlyInflationRateForYearToDate(Country.US)).thenReturn(Optional.of(inflationRate));
        when(accountRepository.findAllByUserId(userId)).thenReturn(accounts);

        accountService.recalculateNetAmountPerUser(userId);

        verify(accountRepository, times(1)).saveAll(accounts);
        verify(eventPublisher, times(1)).publishEvent(any(RetirementGoalEvent.class));
    }

    // Handles scenario where no accounts are found for the user
    @Test
    void test_recalculate_net_amount_no_accounts_found_for_user() {
        Long userId = 1L;
        UserDTO userDTO = new UserDTO(userId, "testuser", Country.US);
        InflationRate inflationRate = InflationRate.builder().percentageRate(BigDecimal.valueOf(2)).country(Country.US).build();
        List<Account> accounts = Collections.emptyList();

        when(userService.findById(userId)).thenReturn(userDTO);
        when(inflationService.fetchLatestMonthlyInflationRateForYearToDate(Country.US)).thenReturn(Optional.of(inflationRate));
        when(accountRepository.findAllByUserId(userId)).thenReturn(accounts);

        accountService.recalculateNetAmountPerUser(userId);

        verify(accountRepository, times(1)).saveAll(accounts);
        verify(eventPublisher, times(1)).publishEvent(any(RetirementGoalEvent.class));
    }

    @Test
    void test_recalculate_net_amount_no_inflation_rate() {
        Long userId = 1L;
        UserDTO userDTO = new UserDTO(userId, "testuser", Country.US);
        Account account1 = Account.builder().id(1L).amount(BigDecimal.valueOf(100)).userId(userId).build();
        Account account2 = Account.builder().id(2L).amount(BigDecimal.valueOf(200)).userId(userId).build();
        List<Account> accounts = Arrays.asList(account1, account2);

        when(userService.findById(userId)).thenReturn(userDTO);
        when(inflationService.fetchLatestMonthlyInflationRateForYearToDate(Country.US)).thenReturn(Optional.empty());

        accountService.recalculateNetAmountPerUser(userId);

        verify(accountRepository, never()).saveAll(accounts);
        verify(eventPublisher, never()).publishEvent(any(RetirementGoalEvent.class));
    }

}
