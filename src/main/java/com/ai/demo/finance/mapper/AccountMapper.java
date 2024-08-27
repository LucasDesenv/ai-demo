package com.ai.demo.finance.mapper;

import com.ai.demo.finance.dto.AccountDTO;
import com.ai.demo.finance.model.Account;
import java.time.LocalDateTime;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(imports = {LocalDateTime.class})
public interface AccountMapper {

    AccountDTO toAccountDTO(Account account);

    Account toAccount(AccountDTO accountDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", source = "userId")
    Account toAccountToCreate(AccountDTO accountDTO, Long userId);
}
