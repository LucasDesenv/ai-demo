package com.ai.demo.finance.model.repository;

import com.ai.demo.finance.model.Account;
import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface AccountRepository extends CrudRepository<Account, Long> {
    List<Account> findAllByUserId(Long userId);
}
