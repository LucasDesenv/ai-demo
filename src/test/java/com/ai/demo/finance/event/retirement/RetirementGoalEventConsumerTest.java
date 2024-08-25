
package com.ai.demo.finance.event.retirement;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ai.demo.finance.event.EventSource;
import com.ai.demo.finance.model.RetirementDetail;
import com.ai.demo.finance.model.repository.RetirementRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RetirementGoalEventConsumerTest {

    @Mock
    private RetirementRepository retirementRepository;

    @Mock
    private RetirementGoalCalculator retirementGoalCalculator;

    @InjectMocks
    private RetirementGoalEventConsumer retirementGoalEventConsumer;

    @Test
    void test_event_processed_when_retirement_event_received() {
        Long userId = 1L;
        RetirementGoalEvent event = new RetirementGoalEvent(userId, EventSource.RETIREMENT_UPDATE);
        RetirementDetail retirementDetail = mock(RetirementDetail.class);

        when(retirementRepository.findByUserId(userId)).thenReturn(Optional.of(retirementDetail));

        retirementGoalEventConsumer.processEvent(event);

        verify(retirementRepository).findByUserId(userId);
        verify(retirementGoalCalculator).calculateRetirementGoal(retirementDetail);
    }

    @Test
    void test_retirement_detail_not_found_for_user_id() {
        Long userId = 1L;
        RetirementGoalEvent event = new RetirementGoalEvent(userId, EventSource.RETIREMENT_UPDATE);

        when(retirementRepository.findByUserId(userId)).thenReturn(Optional.empty());

        retirementGoalEventConsumer.processEvent(event);

        verify(retirementRepository).findByUserId(userId);
        verify(retirementGoalCalculator, never()).calculateRetirementGoal(any());
    }
}