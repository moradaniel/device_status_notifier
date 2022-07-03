package com.devicestatus.mapper;

import com.devicestatus.domain.DeviceStatusEvent;
import com.devicestatus.domain.DeviceStatusEventAvro;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper( uses = {},componentModel = "spring")
public abstract class AvroPersistenceMapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(AvroPersistenceMapper.class);

    @Autowired
    ObjectMapper objectMapper;


    @Mappings({
    })
    public abstract DeviceStatusEventAvro toAvro(DeviceStatusEvent deviceStatusEvent) throws JsonProcessingException;

    @Named("convertObjectToJson")
    public String convertObjectToJson(Object o) throws JsonProcessingException{
        if(o == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException ex) {
            LOGGER.error("JSON processing error. ", ex);
            throw ex;
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
