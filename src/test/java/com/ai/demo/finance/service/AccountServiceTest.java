package com.ai.demo.finance.service;

import static com.ai.demo.finance.model.enums.AccountType.SAVINGS;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.ai.demo.finance.dto.AccountDTO;
import com.ai.demo.finance.exception.NotFoundResourceException;
import com.ai.demo.finance.mapper.AccountMapper;
import com.ai.demo.finance.model.Account;
import com.ai.demo.finance.model.repository.AccountRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    public static final AccountMapper MAPPER = Mappers.getMapper(AccountMapper.class);
    @Mock
    private AccountRepository accountRepository;
    @InjectMocks
    private AccountService accountService;

    @Test
    public void test_create_account_success() {
        // Arrange
        AccountDTO dto = new AccountDTO(1L, new BigDecimal("1000"), SAVINGS, LocalDateTime.now());

        // Act & Assert
        accountService.createAccount(dto);
    }

    @Test
    public void test_find_account_not_found() {
        // Arrange
        Long id = 1L;

        when(accountRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundResourceException.class, () -> {
            accountService.findById(id);
        });
    }

    @Test
    public void test_finding_existing_account_by_id() {
        // Arrange
        Long id = 1L;
        Account account = Account.builder().id(id).amount(new BigDecimal("1000")).type(SAVINGS).date(LocalDateTime.now()).build();

        when(accountRepository.findById(id)).thenReturn(Optional.of(account));

        // Act & Assert
        accountService.findById(id);
    }

    @Test
    public void test_update_account_success() {
        // Arrange
        Long id = 1L;
        AccountDTO dto = new AccountDTO(id, new BigDecimal("2000"), SAVINGS, LocalDateTime.now());

        when(accountRepository.existsById(id)).thenReturn(true);

        // Act & Assert
        accountService.updateAccount(id, dto);
    }

    @Test
    public void test_update_account_not_found() {
        // Arrange
        Long id = 1L;
        AccountDTO dto = new AccountDTO(id, new BigDecimal("2000"), SAVINGS, LocalDateTime.now());

        when(accountRepository.existsById(id)).thenReturn(false);

        // Act & Assert
        assertThrows(NotFoundResourceException.class, () -> {
            accountService.updateAccount(id, dto);
        });
    }

    @Test
    public void test_delete_account_success() {
        // Arrange
        Long id = 1L;

        when(accountRepository.existsById(id)).thenReturn(true);

        // Act & Assert
        accountService.deleteAccount(id);
    }

    @Test
    public void test_delete_account_not_found() {
        // Arrange
        Long id = 1L;

        when(accountRepository.existsById(id)).thenReturn(false);

        // Act & Assert
        assertThrows(NotFoundResourceException.class, () -> {
            accountService.deleteAccount(id);
        });
    }
}
