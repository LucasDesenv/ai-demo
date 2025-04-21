package com.ai.demo.finance.model.cache;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode(of = {"userId"})
public class RetirementGoal implements Serializable {
    private Long userId;
    private BigDecimal goalPercentage;
    private BigDecimal currentNetWorth;
    private List<String> advices;

    public RetirementGoal(Long userId, BigDecimal goalPercentage, BigDecimal currentNetWorth) {
        this.userId = userId;
        this.goalPercentage = goalPercentage;
        this.currentNetWorth = currentNetWorth;
        this.advices = new ArrayList<>();
    }

    @JsonIgnore
    public String getKey() {
        return userId.toString();
    }

    public void addAdvice(String advice) {
        if (advice != null && !advice.isEmpty()) {
            this.advices.add(advice);
        }
    }
}
