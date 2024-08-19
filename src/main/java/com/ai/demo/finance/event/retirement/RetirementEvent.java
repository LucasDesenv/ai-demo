package com.ai.demo.finance.event.retirement;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
@EqualsAndHashCode(of = {"userId"})
public class RetirementEvent {
    private Long userId;
}
