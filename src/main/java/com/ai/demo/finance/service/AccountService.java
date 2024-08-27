package com.ai.demo.finance.service;

import com.ai.demo.finance.dto.AccountDTO;
import com.ai.demo.finance.dto.BalanceDTO;
import com.ai.demo.finance.dto.UserDTO;
import com.ai.demo.finance.event.EventSource;
import com.ai.demo.finance.event.account.AccountNetAmountPerUserEvent;
import com.ai.demo.finance.event.retirement.RetirementGoalEvent;
import com.ai.demo.finance.exception.NotFoundResourceException;
import com.ai.demo.finance.mapper.AccountMapper;
import com.ai.demo.finance.model.Account;
import com.ai.demo.finance.model.AccountHistory;
import com.ai.demo.finance.model.cache.InflationRate;
import com.ai.demo.finance.model.enums.Country;
import com.ai.demo.finance.model.repository.AccountHistoryRepository;
import com.ai.demo.finance.model.repository.AccountRepository;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.mapstruct.factory.Mappers;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Log4j2
public class AccountService {

    private static final AccountMapper MAPPER = Mappers.getMapper(AccountMapper.class);
    private final AccountRepository accountRepository;
    private final AccountHistoryRepository historyRepository;
    private final UserService userService;
    private final InflationService inflationService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public AccountDTO createAccount(AccountDTO accountDTO) {
        UserDTO user = userService.findByUsername(accountDTO.username());
        Account entity = MAPPER.toAccountToCreate(accountDTO, user.id());
        eventPublisher.publishEvent(new AccountNetAmountPerUserEvent(user.id(), EventSource.ACCOUNT_CREATION));
        return MAPPER.toAccountDTO(accountRepository.save(entity));
    }

    public AccountDTO findById(Long id) {
        return accountRepository.findById(id)
                .map(MAPPER::toAccountDTO)
                .orElseThrow(() -> new NotFoundResourceException("Account not found"));
    }

    public AccountDTO updateAccount(Long id, AccountDTO accountDTO) {
        if (accountRepository.existsById(id)) {
            Account account = MAPPER.toAccount(accountDTO);
            Account saved = accountRepository.save(account);
            return MAPPER.toAccountDTO(saved);
        }

        throw new NotFoundResourceException("Account not found with id " + id);
    }

    public void deleteAccount(Long id) {
        if (!accountRepository.existsById(id)) {
            throw new NotFoundResourceException("Account not found with id " + id);
        }

        accountRepository.deleteById(id);
    }

    @Transactional
    public AccountDTO deposit(Long id, BalanceDTO balanceDTO) {
        final Account account = accountRepository.findById(id).orElseThrow(() -> new NotFoundResourceException("Account not found"));
        AccountHistory history = account.deposit(balanceDTO.amount());
        Account savedAccount = accountRepository.save(account);
        historyRepository.save(history);
        eventPublisher.publishEvent(new AccountNetAmountPerUserEvent(savedAccount.getUserId(), EventSource.DEPOSIT));
        return MAPPER.toAccountDTO(savedAccount);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public void recalculateNetAmountPerUser(Long userId) {
        UserDTO user = userService.findById(userId);
        Country country = user.country();
        Optional<InflationRate> inflationRateOpt = inflationService.fetchLatestMonthlyInflationRateForYearToDate(country);

        if (inflationRateOpt.isEmpty()) {
            log.info("No Inflation rate found for country {}. Hence no updating accounts' amount", country);
            return;
        }

        List<Account> accounts = accountRepository.findAllByUserId(userId);
        InflationRate latestInflationRate = inflationRateOpt.get();

        accounts.forEach(account -> account.calculateNetAmount(latestInflationRate));
        accountRepository.saveAll(accounts);
        eventPublisher.publishEvent(new RetirementGoalEvent(userId, EventSource.RECALCULATION_NET_AMOUNT));
    }
}