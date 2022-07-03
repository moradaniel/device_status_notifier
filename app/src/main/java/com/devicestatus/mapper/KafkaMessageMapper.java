package com.devicestatus.mapper;

import com.devicestatus.domain.DeviceStatusEventAvro;
import com.devicestatus.faker.payload.FakeDeviceStatusEventRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.mapstruct.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(builder = @Builder(disableBuilder = true))
public abstract class KafkaMessageMapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaMessageMapper.class);

    @Autowired
    ObjectMapper objectMapper;


    @Mappings({
    })
    public abstract DeviceStatusEventAvro toAvro(FakeDeviceStatusEventRequest request) throws JsonProcessingException;



    @Named("convertObjectToJson")
    public String convertObjectToJson(Object o) throws JsonProcessingException{
        if(o == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException ex) {
            LOGGER.error("JSON processing error. ", ex);
            throw ex;//new RuntimeException(ex);
        }
    }

    @Named("convertJsonToObject")
    public <T> T convertJsonToObject(String json, Class<T> type){
        if(json == null) {
            return null;
        }

        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException ex) {
            LOGGER.error("JSON processing error. ", ex);
            throw new RuntimeException(ex);
        }
    }
}
