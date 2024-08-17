package com.ai.demo.finance.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "account_history")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private BigDecimal amount;
    private ZonedDateTime date;

    @Column(nullable = false, updatable = false)
    private Long accountId;

    public AccountHistory(Account account) {
        this.accountId = account.getId();
        this.amount = account.getAmount();
        this.date = ZonedDateTime.now();
    }
}
