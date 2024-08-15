package com.ai.demo.finance.mapper;

import com.ai.demo.finance.dto.AccountDTO;
import com.ai.demo.finance.model.Account;
import org.mapstruct.Mapper;

@Mapper
public interface AccountMapper {

    AccountDTO toAccountDTO(Account account);

    Account toAccount(AccountDTO accountDTO);
}
