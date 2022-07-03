package com.devicestatus.config;

import io.github.resilience4j.retry.RetryConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;


@Configuration
@Slf4j
public class NotifyEndpointRetryStrategyConfig {

    @Value("${partners.notifyStatus.retryMaxAttempts}")
    private Integer retryMaxAttempts;

    @Value("${partners.notifyStatus.retryDurationBetweenAttemptsMilliseconds}")
    private Integer retryDurationBetweenAttemptsMilliseconds;

    @Bean(name = "notifyEndpointRetryConfig")
    public RetryConfig notifyEndpointRetryConfig() {


        RetryConfig config = RetryConfig.custom()
                .maxAttempts(retryMaxAttempts)
                //waitDuration: A fixed wait duration between retry attempts
                .waitDuration(Duration.of(retryDurationBetweenAttemptsMilliseconds, ChronoUnit.MILLIS))
                //.intervalFunction(intervalFn)
                /*
                retryExceptions:Configures a list of Throwable classes that are recorded as a failure and thus are retried. This parameter supports subtyping.
                    Note: If you are using Checked Exceptions you must use a CheckedSupplier
                */
                .retryExceptions(ResourceAccessException.class, IOException.class)
                .ignoreExceptions(HttpServerErrorException.class, HttpClientErrorException.class)
                .build();

        return config;
    }

}
