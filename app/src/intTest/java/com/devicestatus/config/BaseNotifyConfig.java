package com.devicestatus.config;


import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

@Profile("intTest")
@Configuration
public class BaseNotifyConfig {

    int connectTimeoutMilliseconds = 1000;
    int readTimeoutMilliseconds = 1000;
    int writeTimeoutMilliseconds = 1000;

    @Bean(name = "notifyEndpointRestTemplate")
    public RestTemplate okhttp3Template() throws Exception{
        RestTemplate restTemplate = new RestTemplate();
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        builder.connectTimeout(connectTimeoutMilliseconds, TimeUnit.MILLISECONDS);
        builder.readTimeout(readTimeoutMilliseconds, TimeUnit.MILLISECONDS);
        builder.writeTimeout(writeTimeoutMilliseconds, TimeUnit.MILLISECONDS);

        // embed the created okhttp client to a spring rest template
        restTemplate.setRequestFactory(new OkHttp3ClientHttpRequestFactory(builder.build()));
        return restTemplate;
    }

}
