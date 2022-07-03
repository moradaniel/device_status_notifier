package com.devicestatus.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;


@Configuration
public class ObjectMapperConfig {

    @Autowired
    Environment env;

    @Bean
    ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();
        //objectMapper.configure(
        //        SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(
                SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        objectMapper.configure(
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return objectMapper;
    }


}
