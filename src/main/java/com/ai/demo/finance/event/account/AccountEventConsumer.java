package com.ai.demo.finance.event.account;

import com.ai.demo.finance.event.EventSource;
import com.ai.demo.finance.event.retirement.RetirementGoalEvent;
import com.ai.demo.finance.service.AccountService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@AllArgsConstructor
@Log4j2
public class AccountEventConsumer {

    private final AccountService accountService;
    private final ApplicationEventPublisher eventPublisher;

    @TransactionalEventListener(condition = "#event.source != T(com.ai.demo.finance.event.EventSource).SCAN")
    public void recalculateNetAmountInsideTransaction(AccountEvent event) {
        processEvent(event);
    }

    @EventListener(condition = "#event.source == T(com.ai.demo.finance.event.EventSource).SCAN")
    public void recalculateNetAmountOutsideTransaction(AccountEvent event) {
        processEvent(event);
    }

    private void processEvent(AccountEvent event) {
        log.info("AccountEventConsumer: {}", event);
        switch (event.source()) {
            case ACCOUNT_CREATION -> eventPublisher.publishEvent(new RetirementGoalEvent(event.userId(), EventSource.ACCOUNT_CREATION));
            case RECALCULATION_NET_AMOUNT, DEPOSIT, SCAN, RETIREMENT_UPDATE -> accountService.recalculateNetAmountPerUser(event.userId());
            default -> log.warn("AccountEventConsumer unknown source: {}", event.source());
        }
    }

}
