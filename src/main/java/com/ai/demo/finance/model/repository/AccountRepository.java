package com.ai.demo.finance.model.repository;

import com.ai.demo.finance.model.Account;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface AccountRepository extends JpaRepository<Account, Long>, PagingAndSortingRepository<Account, Long> {
    List<Account> findAllByUserId(Long userId);
}
