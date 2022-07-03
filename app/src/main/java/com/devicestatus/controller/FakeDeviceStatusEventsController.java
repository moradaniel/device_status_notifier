package com.devicestatus.controller;


import com.devicestatus.domain.DeviceStatusEventAvro;
import com.devicestatus.faker.payload.FakeDeviceStatusEventRequest;
import com.devicestatus.mapper.KafkaMessageMapper;
import com.devicestatus.util.Utils;
import com.devicestatus.web.payload.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Arrays;

@Slf4j
@RestController
@RequestMapping(value = "/api/fake/devicestatus")
public class FakeDeviceStatusEventsController {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private KafkaMessageMapper kafkaMessageMapper;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.device-status-events}")
    private String deviceStatusEventsTopic;

    @Autowired
    public FakeDeviceStatusEventsController() {
    }

    @PostMapping("")
    @Operation(description = "Generate fake device status events")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Generate fake device status events", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = FakeDeviceStatusEventRequest.class))
            })
    })
    public ResponseEntity<ApiResponse<FakeDeviceStatusEventRequest>> fakeDeviceStatusEvent(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "FakeDeviceStatusEventRequest. Cannot be null or empty.",
                    required = true, content = @Content(schema = @Schema(implementation = FakeDeviceStatusEventRequest.class)))
            @RequestBody @Valid FakeDeviceStatusEventRequest fakeDeviceStatusEventRequest) {

        log.info("Attempting to send  DeviceStatusEventRequest for deviceId={}", fakeDeviceStatusEventRequest.getDeviceId());

        ApiResponse<FakeDeviceStatusEventRequest> apiResponse = new ApiResponse();
        try {

            publishToDeviceStatusEventsTopic(fakeDeviceStatusEventRequest);


            apiResponse.setResponse(fakeDeviceStatusEventRequest);
        } catch (Exception ex) {

            String errorMessage = "Error in FakeDeviceStatusEventsController for deviceId=" + fakeDeviceStatusEventRequest.getDeviceId();
            String exceptionLogEntry = Utils.makeExceptionLogEntry(ex, errorMessage, String.valueOf(fakeDeviceStatusEventRequest.getDeviceId()));
            log.error(exceptionLogEntry);

            apiResponse.setErrors(Arrays.asList(exceptionLogEntry));

            return new ResponseEntity<>(apiResponse, HttpStatus.SERVICE_UNAVAILABLE);
        }

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);

    }


    private void publishToDeviceStatusEventsTopic(FakeDeviceStatusEventRequest event) {
        try {
            DeviceStatusEventAvro deviceStatusEventAvro = kafkaMessageMapper.toAvro(event);
            if (log.isDebugEnabled()) {
                log.debug("DeviceStatusEventAvro = {} for deviceId={}", deviceStatusEventAvro, deviceStatusEventAvro.getDeviceId());
            }
            kafkaTemplate.send(deviceStatusEventsTopic, event.getDeviceId().toString(), deviceStatusEventAvro);
            log.info("published device status event to topic={}.  deviceId={} ", deviceStatusEventsTopic,
                    event.getDeviceId(), deviceStatusEventAvro);
        } catch (Exception exception) {
            log.error("Error in publishing device status event to topic={}.  deviceId={} with error={}", deviceStatusEventsTopic, event.getDeviceId(), exception.getMessage());
        }
    }

}