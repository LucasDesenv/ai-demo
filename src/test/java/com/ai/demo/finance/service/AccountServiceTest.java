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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ai.demo.finance.dto.AccountDTO;
import com.ai.demo.finance.dto.BalanceDTO;
import com.ai.demo.finance.dto.UserDTO;
import com.ai.demo.finance.event.retirement.RetirementEvent;
import com.ai.demo.finance.exception.NotFoundResourceException;
import com.ai.demo.finance.model.Account;
import com.ai.demo.finance.model.AccountHistory;
import com.ai.demo.finance.model.repository.AccountHistoryRepository;
import com.ai.demo.finance.model.repository.AccountRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    @InjectMocks
    private AccountService accountService;

    @Test
    void test_create_account_success() {
        // Arrange
        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        AccountDTO dto = new AccountDTO(1L, new BigDecimal("1000"), SAVINGS, LocalDateTime.now(), "john");

        when(userService.findByUsername("john")).thenReturn(new UserDTO(2L, "john"));
        when(accountRepository.save(accountCaptor.capture())).then(AdditionalAnswers.returnsFirstArg());

        // Act & Assert
        AccountDTO created = accountService.createAccount(dto);
        assertEquals(dto.amount(), created.amount());
        assertEquals(dto.type(), created.type());
        assertNotEquals(dto.id(), created.id());

        Account accountCaptorValue = accountCaptor.getValue();
        assertNull(accountCaptorValue.getId());
        verify(eventPublisher).publishEvent(new RetirementEvent(2L));
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
        AccountDTO dto = new AccountDTO(id, new BigDecimal("2000"), SAVINGS, LocalDateTime.now(), null);

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
        AccountDTO dto = new AccountDTO(id, new BigDecimal("2000"), SAVINGS, LocalDateTime.now(), "john");

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
        Account account = new Account(accountId, new BigDecimal("200.00"), SAVINGS, LocalDateTime.now(), userId);
        AccountHistory accountHistory = new AccountHistory(account);
        Account updatedAccount = new Account(accountId, new BigDecimal("300.00"), SAVINGS, LocalDateTime.now(), userId);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(updatedAccount);
        when(historyRepository.save(any(AccountHistory.class))).thenReturn(accountHistory);

        AccountDTO result = accountService.deposit(accountId, balanceDTO);

        assertNotNull(result);
        assertEquals(updatedAccount.getAmount(), result.amount());
        verify(accountRepository).save(any(Account.class));
        verify(historyRepository).save(any(AccountHistory.class));
        verify(eventPublisher).publishEvent(new RetirementEvent(userId));
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
        verify(eventPublisher, never()).publishEvent(any(RetirementEvent.class));
    }

}
