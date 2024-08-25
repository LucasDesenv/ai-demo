package com.ai.demo.finance.model.cache;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
@EqualsAndHashCode(of = {"userId"})
public class RetirementGoal implements Serializable {
    private Long userId;
    private BigDecimal goalPercentage;

    @JsonIgnore
    public String getKey() {
        return userId.toString();
    }
}
