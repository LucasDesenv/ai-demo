package com.ai.demo.finance.event.account;

import com.ai.demo.finance.event.EventSource;

public record AccountNetAmountPerUserEvent(Long userId, EventSource source) {
}
