package com.ai.demo.finance.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ai.demo.finance.config.ObjectMapperConfig;
import com.ai.demo.finance.event.account.AccountNetAmountPerUserEvent;
import com.ai.demo.finance.model.User;
import com.ai.demo.finance.model.cache.InflationRate;
import com.ai.demo.finance.model.cache.InflationRateKeyGenerator;
import com.ai.demo.finance.model.enums.Country;
import com.ai.demo.finance.model.external.imf.IFSResponse;
import com.ai.demo.finance.model.external.imf.Indicator;
import com.ai.demo.finance.model.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class InflationServiceTest {
    @Mock
    private RestTemplate restTemplate;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RedisTemplate<String, InflationRate> redisTemplate;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private UserRepository userRepository;

    private InflationService inflationService;

    @BeforeEach
    public void setUp() {
        inflationService = new InflationService(restTemplate, "http://imf",
                redisTemplate,
                eventPublisher, userRepository);
    }

    // Retrieves the inflation rate for the current month if available in the cache
    @Test
    void test_retrieves_inflation_rate_for_current_month_if_available_in_cache() {
        // Arrange
        Country country = Country.US;
        LocalDate now = LocalDate.now();
        String key = InflationRateKeyGenerator.generateKey(country, now);
        InflationRate expectedRate = new InflationRate(BigDecimal.valueOf(2.5), now.toString(), country, Indicator.PCPI_IX);

        when(redisTemplate.opsForValue().get(key)).thenReturn(expectedRate);

        // Act
        Optional<InflationRate> result = inflationService.fetchLatestMonthlyInflationRateForYearToDate(country);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(expectedRate, result.get());
    }

    // Retrieves the inflation rate for the current month if available in the cache
    @Test
    void test_retrieves_inflation_rate_for_previous_month_if_available_in_cache() {
        // Arrange
        Country country = Country.US;
        LocalDate now = LocalDate.now();
        String key = InflationRateKeyGenerator.generateKey(country, now);
        String previousMonthKey = InflationRateKeyGenerator.generateKey(country, now.minusMonths(1));
        InflationRate expectedRate = new InflationRate(BigDecimal.valueOf(2.5), now.toString(), country, Indicator.PCPI_IX);

        when(redisTemplate.opsForValue().get(key)).thenReturn(null);
        when(redisTemplate.opsForValue().get(previousMonthKey)).thenReturn(expectedRate);

        // Act
        Optional<InflationRate> result = inflationService.fetchLatestMonthlyInflationRateForYearToDate(country);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(expectedRate, result.get());
    }

    // Handles the scenario where the Redis cache is empty
    @Test
    void test_handles_scenario_where_redis_cache_is_empty() {
        // Arrange
        Country country = Country.US;
        LocalDate now = LocalDate.now();
        String key = InflationRateKeyGenerator.generateKey(country, now);
        String previousMonthKey = InflationRateKeyGenerator.generateKey(country, now.minusMonths(1L));

        when(redisTemplate.opsForValue().get(key)).thenReturn(null);
        when(redisTemplate.opsForValue().get(previousMonthKey)).thenReturn(null);

        // Act
        Optional<InflationRate> result = inflationService.fetchLatestMonthlyInflationRateForYearToDate(country);

        // Assert
        assertFalse(result.isPresent());
    }

    // Handles empty IMF data response gracefully
    @Test
    void test_handle_scan_empty_imf_data_response() {
        when(restTemplate.getForEntity(Mockito.any(URI.class), Mockito.eq(IFSResponse.class)))
                .thenReturn(ResponseEntity.of(Optional.empty()));

        inflationService.scan();

        verify(restTemplate, times(Country.values().length))
                .getForEntity(Mockito.any(URI.class), Mockito.eq(IFSResponse.class));
        Mockito.verifyNoInteractions(redisTemplate, eventPublisher, userRepository);
    }

    @Test
    void test_handle_scan_publish_retirement_goal_event() throws IOException {

        ObjectMapper objectMapper = new ObjectMapperConfig().objectMapper();
        IFSResponse ifsResponse = objectMapper.readValue(
                InflationServiceTest.class.getResourceAsStream("/json/ifs-valid-response.json"), IFSResponse.class);

        ArgumentCaptor<AccountNetAmountPerUserEvent> captor = ArgumentCaptor.forClass(
                AccountNetAmountPerUserEvent.class);

        when(restTemplate.getForEntity(Mockito.any(URI.class), Mockito.eq(IFSResponse.class)))
                .thenReturn(ResponseEntity.of(Optional.of(ifsResponse)));
        when(userRepository.findAllByCountry(any(), any()))
                .thenReturn(new PageImpl<>(Arrays.asList(
                        User.builder().id(98939L).build(),
                        User.builder().id(1233L).build())));

        inflationService.scan();

        verify(restTemplate, times(Country.values().length))
                .getForEntity(Mockito.any(URI.class), Mockito.eq(IFSResponse.class));
        verify(eventPublisher, times(6)).publishEvent(captor.capture());

        List<AccountNetAmountPerUserEvent> allEvents = captor.getAllValues();
        Assertions.assertThat(allEvents).extracting("userId").containsOnlyElementsOf(Arrays.asList(98939L, 1233L));
    }

    @Test
    void test_handle_scan_cache_into_redis() throws IOException {

        ObjectMapper objectMapper = new ObjectMapperConfig().objectMapper();
        IFSResponse ifsResponse = objectMapper.readValue(
                InflationServiceTest.class.getResourceAsStream("/json/ifs-valid-response.json"), IFSResponse.class);

        when(restTemplate.getForEntity(Mockito.any(URI.class), Mockito.eq(IFSResponse.class)))
                .thenReturn(ResponseEntity.of(Optional.of(ifsResponse)));
        when(userRepository.findAllByCountry(any(), any()))
                .thenReturn(new PageImpl<>(Arrays.asList(
                        User.builder().id(98939L).build(),
                        User.builder().id(1233L).build())));

        inflationService.scan();

        verify(restTemplate, times(Country.values().length))
                .getForEntity(Mockito.any(URI.class), Mockito.eq(IFSResponse.class));
        verify(redisTemplate.opsForValue(), times(Country.values().length)).set(anyString(), any(InflationRate.class));
    }

}
