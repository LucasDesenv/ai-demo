package com.ai.demo.finance.service;

import com.ai.demo.finance.dto.AccountDTO;
import com.ai.demo.finance.dto.BalanceDTO;
import com.ai.demo.finance.exception.NotFoundResourceException;
import com.ai.demo.finance.mapper.AccountMapper;
import com.ai.demo.finance.model.Account;
import com.ai.demo.finance.model.AccountHistory;
import com.ai.demo.finance.model.repository.AccountHistoryRepository;
import com.ai.demo.finance.model.repository.AccountRepository;
import lombok.AllArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class AccountService {

    private static final AccountMapper MAPPER = Mappers.getMapper(AccountMapper.class);
    private final AccountRepository accountRepository;
    private final AccountHistoryRepository historyRepository;

    public AccountDTO createAccount(AccountDTO accountDTO) {
        Account entity = MAPPER.toAccountToCreate(accountDTO);
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
        return MAPPER.toAccountDTO(savedAccount);
    }
}