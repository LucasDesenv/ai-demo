package com.ai.demo.finance.model;

import com.ai.demo.finance.exception.InvalidOperationException;
import com.ai.demo.finance.model.cache.InflationRate;
import com.ai.demo.finance.model.enums.AccountType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(name = "amount_gross")
    private BigDecimal amount;
    @Column(name = "amount_net")
    private BigDecimal amountNet;
    @Column(nullable = false)
    private AccountType type;
    @Column(nullable = false)
    private LocalDateTime date;
    @Column(nullable = false, updatable = false)
    private Long userId;

    /**
     * Adds the specified deposit amount to the account balance.
     * @param deposit the amount to be deposited, must be greater than zero
     * @return the account history before the deposit
     * @throws IllegalArgumentException if the deposit amount is null or less than
     *             or equal to zero
     */
    public AccountHistory deposit(BigDecimal deposit) {
        if (deposit == null || deposit.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOperationException("Amount must be greater than zero");
        }

        AccountHistory history = new AccountHistory(this);

        this.amount = Optional.ofNullable(this.amount)
                .map(a -> a.add(deposit))
                .orElse(deposit);

        return history;
    }

    public void calculateNetAmount(InflationRate inflationRate) {
        this.amountNet = inflationRate.calculateRateFromPercentage().multiply(amount);
    }
}