package com.ai.demo.finance.event.retirement;

import com.ai.demo.finance.model.Account;
import com.ai.demo.finance.model.RetirementDetail;
import com.ai.demo.finance.model.repository.AccountRepository;
import com.ai.demo.finance.model.repository.RetirementRepository;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Log4j2
public class RetirementEventConsumer {

    private final AccountRepository accountRepository;
    private final RetirementRepository retirementRepository;
    private final RetirementGoalCalculator retirementGoalCalculator;

    @EventListener(RetirementEvent.class)
    public void processEvent(RetirementEvent event) {
        log.debug("RetirementEvent received: {}", event);
        Long userId = event.getUserId();
        retirementRepository.findByUserId(userId).ifPresentOrElse((RetirementDetail retirementDetail) -> {
            List<Account> accounts = accountRepository.findAllByUserId(userId);
            retirementGoalCalculator.calculateRetirementGoal(retirementDetail, accounts);
            log.info("Retirement goal calculated for user: {}", event.getUserId());
        }, () -> log.warn("Retirement goal not found for user {}", event.getUserId()));

    }
}
