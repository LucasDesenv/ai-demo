
package com.ai.demo.finance.event.retirement;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ai.demo.finance.model.Account;
import com.ai.demo.finance.model.RetirementDetail;
import com.ai.demo.finance.model.repository.AccountRepository;
import com.ai.demo.finance.model.repository.RetirementRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RetirementEventConsumerTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private RetirementRepository retirementRepository;

    @Mock
    private RetirementGoalCalculator retirementGoalCalculator;

    @InjectMocks
    private RetirementEventConsumer retirementEventConsumer;

    @Test
    void test_event_processed_when_retirement_event_received() {
        Long userId = 1L;
        RetirementEvent event = new RetirementEvent(userId);
        RetirementDetail retirementDetail = mock(RetirementDetail.class);
        List<Account> accounts = List.of(mock(Account.class));

        when(retirementRepository.findByUserId(userId)).thenReturn(Optional.of(retirementDetail));
        when(accountRepository.findAllByUserId(userId)).thenReturn(accounts);

        retirementEventConsumer.processEvent(event);

        verify(retirementRepository).findByUserId(userId);
        verify(accountRepository).findAllByUserId(userId);
        verify(retirementGoalCalculator).calculateRetirementGoal(retirementDetail, accounts);
    }

    @Test
    void test_retirement_detail_not_found_for_user_id() {
        Long userId = 1L;
        RetirementEvent event = new RetirementEvent(userId);

        when(retirementRepository.findByUserId(userId)).thenReturn(Optional.empty());

        retirementEventConsumer.processEvent(event);

        verify(retirementRepository).findByUserId(userId);
        verify(accountRepository, never()).findAllByUserId(userId);
        verify(retirementGoalCalculator, never()).calculateRetirementGoal(any(), any());
    }
}