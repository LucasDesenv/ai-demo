package com.ai.demo.finance.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ai.demo.finance.exception.InvalidOperationException;
import com.ai.demo.finance.exception.NotFoundResourceException;
import com.ai.demo.finance.model.cache.RetirementGoal;
import java.math.BigDecimal;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class RetirementGoalServiceTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RedisTemplate<String, RetirementGoal> redisTemplate;
    private RetirementGoalService retirementGoalService;

    @BeforeEach
    public void setUp() {
        retirementGoalService = new RetirementGoalService(redisTemplate, 1L);
    }

    // Successfully saves a valid RetirementGoal object to Redis
    @Test
    void test_save_valid_retirement_goal() {
        // Arrange
        ValueOperations<String, RetirementGoal> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        RetirementGoal retirementGoal = new RetirementGoal(1L, new BigDecimal("0.15"));

        // Act
        retirementGoalService.saveRetirementGoal(retirementGoal);

        // Assert
        verify(valueOperations).set("1", retirementGoal, Duration.ofMinutes(1L));
    }

    // Handles null RetirementGoal object gracefully
    @Test
    void test_save_null_retirement_goal() {
        assertThrows(InvalidOperationException.class, () -> retirementGoalService.saveRetirementGoal(null));
    }

    @Test
    void test_retrieve_existing_retirement_goal() {
        // Arrange
        Long userId = 1L;
        RetirementGoal expectedGoal = new RetirementGoal(userId, new BigDecimal("0.15"));
        when(redisTemplate.opsForValue().get(String.valueOf(userId))).thenReturn(expectedGoal);

        // Act
        RetirementGoal actualGoal = retirementGoalService.getRetirementGoal(userId);

        // Assert
        assertEquals(expectedGoal, actualGoal);
    }

    // Handle null userId input gracefully
    @Test
    void test_handle_null_user_id() {
        // Arrange
        Long userId = 1L;
        when(redisTemplate.opsForValue().get(String.valueOf(userId))).thenReturn(null);

        // Act & Assert
        assertThrows(NotFoundResourceException.class, () -> retirementGoalService.getRetirementGoal(userId));
    }
}
