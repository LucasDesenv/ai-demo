package com.ai.demo.finance.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
public class ObjectMapperConfig {

    /**
     * Jackson builder.
     * @return the jackson2 object mapper builder
     */
    @Bean
    public Jackson2ObjectMapperBuilder jacksonBuilder() {
        final var builder = new Jackson2ObjectMapperBuilder();
        builder.indentOutput(false);
        builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return builder;
    }

    /**
     * Creates a JavaTimeModule
     * @return JavaTimeModule
     */
    private JavaTimeModule customJavaTimeModule() {
        return new JavaTimeModule();
    }

    /**
     * Default Object Mapper.
     * @return ObjectMapper
     */
    @Bean(name = "objectMapper")
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper builder = jacksonBuilder()
                .simpleDateFormat("yyyy-MM-dd")
                .modules(customJavaTimeModule())
                .build();
        builder.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        builder.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        builder.setFilterProvider(new SimpleFilterProvider().setFailOnUnknownId(false));

        return builder;
    }
}
