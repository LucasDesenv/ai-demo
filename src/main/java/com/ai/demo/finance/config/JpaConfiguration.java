package com.ai.demo.finance.config;

import com.ai.demo.finance.model.repository.RetirementRepository;
import com.ai.demo.travel.model.repository.UserProfileRepository;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackageClasses = {RetirementRepository.class, UserProfileRepository.class})
@EntityScan(basePackages = {"com.ai.demo.finance.model", "com.ai.demo.travel.model"})
public class JpaConfiguration {

}
