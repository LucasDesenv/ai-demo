package com.ai.demo.finance.service;

import static com.ai.demo.finance.model.cache.InflationRateKeyGenerator.generateKey;

import com.ai.demo.finance.event.EventSource;
import com.ai.demo.finance.event.account.AccountNetAmountPerUserEvent;
import com.ai.demo.finance.model.User;
import com.ai.demo.finance.model.cache.InflationRate;
import com.ai.demo.finance.model.enums.Country;
import com.ai.demo.finance.model.external.imf.CompactData;
import com.ai.demo.finance.model.external.imf.DataSet;
import com.ai.demo.finance.model.external.imf.IFSResponse;
import com.ai.demo.finance.model.external.imf.Indicator;
import com.ai.demo.finance.model.external.imf.RatePeriod;
import com.ai.demo.finance.model.external.imf.Series;
import com.ai.demo.finance.model.repository.UserRepository;
import java.net.URI;
import java.time.LocalDate;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Log4j2
public class InflationService {

    private final RestTemplate restTemplate;
    private final String ifsBaseUrl;
    private final RedisTemplate<String, InflationRate> redisTemplate;
    private final ApplicationEventPublisher eventPublisher;
    private final UserRepository userRepository;

    public InflationService(RestTemplate restTemplate,
            @Value("${ifs.dataservice.base.url:http://dataservices.imf.org/REST/SDMX_JSON.svc/CompactData/IFS}") String ifsBaseUrl,
            RedisTemplate<String, InflationRate> redisTemplate, ApplicationEventPublisher eventPublisher,
            UserRepository userRepository) {
        this.restTemplate = restTemplate;
        this.ifsBaseUrl = ifsBaseUrl;
        this.redisTemplate = redisTemplate;
        this.eventPublisher = eventPublisher;
        this.userRepository = userRepository;
    }

    public Optional<InflationRate> fetchLatestMonthlyInflationRateForYearToDate(Country country) {
        String key = generateKey(country, LocalDate.now());
        return Optional.ofNullable(redisTemplate.opsForValue().get(key))
                .or(() -> Optional.ofNullable(redisTemplate.opsForValue().get(generateKey(country, LocalDate.now().minusMonths(1L)))));

    }

    private Optional<Series> fetchMonthlyInflationRate(Country country, Integer startPeriod, Integer endPeriod) {

        String frequency = "M";
        String param = frequency.concat(".").concat(country.name()).concat(".")
                .concat(Indicator.PCPI_IX.name())
                .concat("?startPeriod=" + startPeriod)
                .concat("&endPeriod=" + endPeriod);
        URI url = URI.create(ifsBaseUrl.concat("/").concat(param));
        log.debug("Retrieved inflation rate: {}", url);

        ResponseEntity<IFSResponse> response = restTemplate.getForEntity(url, IFSResponse.class);
        log.info("Retrieved inflation rate: {}", response.getBody());

        var body = response.getBody();

        return Optional.ofNullable(body)
                .map(IFSResponse::getCompactData)
                .map(CompactData::getDataSet)
                .map(DataSet::getSeries);
    }

    @Scheduled(cron = "${imf.scheduler.cron}")
    public void scan() {
        log.info("Starting scan");
        LocalDate now = LocalDate.now();
        LocalDate previousMonth = now.minusMonths(1L);

        for (Country country : Country.values()) {

            Optional<Series> imfData = fetchMonthlyInflationRate(country, previousMonth.getYear(), now.getYear());
            if (imfData.isEmpty()) {
                log.error("Could not find inflation rate for country {}", country.name());
                continue;
            }

            Series series = imfData.get();
            RatePeriod ratePeriodCalculated = series.calculateMonthInflationRate();
            InflationRate inflationRate = cacheInflationRate(ratePeriodCalculated, country);
            sendEventToRecalculateRetirementGoals(inflationRate);
        }

    }

    private InflationRate cacheInflationRate(RatePeriod ratePeriod, Country country) {
        InflationRate inflationRate = InflationRate.builder().percentageRate(ratePeriod.getRate())
                .country(country)
                .indicator(Indicator.PCPI_IX).period(ratePeriod.getTimePeriod())
                .build();
        redisTemplate.opsForValue().set(generateKey(inflationRate), inflationRate);
        log.info("Inflation Rate cached: {}", inflationRate);
        return inflationRate;
    }

    private void sendEventToRecalculateRetirementGoals(InflationRate inflationRate) {
        int page = 0;
        int size = 100;
        Page<User> userPage;
        do {
            Pageable pageRequest = PageRequest.of(page, size);
            userPage = userRepository.findAllByCountry(inflationRate.getCountry(), pageRequest);

            userPage.getContent().forEach(user -> eventPublisher.publishEvent(new AccountNetAmountPerUserEvent(user.getId(), EventSource.SCAN)));

            page++;
        } while (userPage.hasNext());
    }
}
