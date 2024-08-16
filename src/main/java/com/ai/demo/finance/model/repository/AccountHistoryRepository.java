package com.ai.demo.finance.model.repository;

import com.ai.demo.finance.model.AccountHistory;
import org.springframework.data.repository.CrudRepository;

public interface AccountHistoryRepository extends CrudRepository<AccountHistory, Long> {
}
