package com.ai.demo.finance.event.account;

import com.ai.demo.finance.event.EventSource;

public record AccountEvent(Long userId, EventSource source) {
}
