package com.ai.demo.finance.event.retirement;

import com.ai.demo.finance.model.RetirementDetail;
import com.ai.demo.finance.model.repository.RetirementRepository;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@AllArgsConstructor
@Log4j2
public class RetirementGoalEventConsumer {

    private final RetirementRepository retirementRepository;
    private final RetirementGoalCalculator retirementGoalCalculator;

    @TransactionalEventListener(RetirementGoalEvent.class)
    public void processEvent(RetirementGoalEvent event) {
        log.debug("RetirementGoalEvent received: {}", event);
        Long userId = event.userId();
        retirementRepository.findByUserId(userId).ifPresentOrElse((RetirementDetail retirementDetail) -> {
            retirementGoalCalculator.calculateRetirementGoal(retirementDetail);
            log.info("Retirement goal calculated for user: {}", event.userId());
        }, () -> log.warn("Retirement goal not found for user {}", event.userId()));

    }
}
