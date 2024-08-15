package com.ai.demo.finance.model.repository;

import com.ai.demo.finance.model.Account;
import org.springframework.data.repository.CrudRepository;

public interface AccountRepository extends CrudRepository<Account, Long> {
}
