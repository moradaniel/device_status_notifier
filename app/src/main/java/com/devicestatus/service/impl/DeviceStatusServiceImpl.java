package com.devicestatus.service.impl;

import com.devicestatus.domain.*;
import com.devicestatus.exception.PersistenceException;
import com.devicestatus.notify.NotifyService;
import com.devicestatus.notify.payload.request.DeviceStatusNotifyRequest;
import com.devicestatus.repository.DeviceStatusEventRepository;
import com.devicestatus.service.DeviceService;
import com.devicestatus.service.DeviceStatusService;
import com.devicestatus.util.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;


import com.devicestatus.mapper.AvroPersistenceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeviceStatusServiceImpl implements DeviceStatusService {

    private final DeviceService deviceService;

    private final DeviceStatusEventRepository deviceStatusEventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.device-status-change-notify}")
    private String deviceStatusChangeNotifyTopic;

    private final AvroPersistenceMapper avroPersistenceMapper;

    private final NotifyService notifyService;

    @Override
    public void process(DeviceStatusEvent deviceStatusEvent) throws Exception {

        Optional<Device> optionalDevice = deviceService.findByDeviceId(Long.valueOf(deviceStatusEvent.getDeviceId()));

        if(optionalDevice.isPresent()){
            DeviceStatus previousStatus = optionalDevice.get().getStatus();
            DeviceStatus newStatus = deviceStatusEvent.getStatus();
            DeviceStatusChange deviceStatusChange = new DeviceStatusChange(optionalDevice.get().getId(), previousStatus, newStatus);
            optionalDevice.get().setStatus(newStatus);
            if(deviceStatusChange.isNotifiable()){
                notifyDeviceStatusChange(deviceStatusEvent);
                try {
                    notifyService.notify(DeviceStatusNotifyRequest.of(deviceStatusEvent));
                }catch(Exception ex){
                    log.error(Utils.makeExceptionLogEntry(ex, ex.getMessage(), String.valueOf(deviceStatusEvent.getDeviceId())));
                }
            }
            persistDeviceOnRelationalDB(optionalDevice.get());
            deviceStatusEventRepository.createDeviceStatusEvent(deviceStatusEvent);
        }else{
            throw new PersistenceException("device with id: "+deviceStatusEvent.getDeviceId() + " does not exist in database");
        }

    }


    public void persistDeviceOnRelationalDB(Device device) throws Exception {
        Device savedDevice = deviceService.save(device);
        log.info("Device saved to database. deviceId={}. status={}", savedDevice.getId(), savedDevice.getStatus().toString());
    }

    public void notifyDeviceStatusChange(DeviceStatusEvent deviceStatusEvent) throws PersistenceException {
        try {
            DeviceStatusEventAvro deviceStatusEventAvro = avroPersistenceMapper.toAvro(deviceStatusEvent);
            kafkaTemplate.send(deviceStatusChangeNotifyTopic, deviceStatusEventAvro.getDeviceId(), deviceStatusEventAvro);
            log.info("Published DeviceStatusEventAvro to topic={}. deviceId={}", deviceStatusChangeNotifyTopic, deviceStatusEventAvro.getDeviceId());
        }
        catch(JsonProcessingException ex){
            throw new PersistenceException(ex.getMessage(), ex);
        }
    }



}
