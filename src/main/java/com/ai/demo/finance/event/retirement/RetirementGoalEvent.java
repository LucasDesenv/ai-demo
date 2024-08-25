package com.ai.demo.finance.event.retirement;

import com.ai.demo.finance.event.EventSource;

public record RetirementGoalEvent(Long userId, EventSource source) {
}
