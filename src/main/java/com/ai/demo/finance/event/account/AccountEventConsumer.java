package com.ai.demo.finance.event.account;

import com.ai.demo.finance.service.AccountService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@AllArgsConstructor
@Log4j2
public class AccountEventConsumer {

    private final AccountService accountService;

    @TransactionalEventListener(condition = "#event.source != T(com.ai.demo.finance.event.EventSource).SCAN")
    public void recalculateNetAmountInsideTransaction(AccountNetAmountPerUserEvent event) {
        processEvent(event);
    }

    @EventListener(condition = "#event.source == T(com.ai.demo.finance.event.EventSource).SCAN")
    public void recalculateNetAmountOutsideTransaction(AccountNetAmountPerUserEvent event) {
        processEvent(event);
    }

    private void processEvent(AccountNetAmountPerUserEvent event) {
        log.info("AccountEventConsumer: {}", event);
        accountService.recalculateNetAmountPerUser(event.userId());
    }

}
