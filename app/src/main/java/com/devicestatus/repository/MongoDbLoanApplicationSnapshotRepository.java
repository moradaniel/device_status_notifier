package com.devicestatus.repository;

import com.devicestatus.domain.DeviceStatusEvent;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class MongoDbLoanApplicationSnapshotRepository implements DeviceStatusEventRepository {

    private SpringDataMongodeviceStatusEventRepository deviceStatusEventRepository;

    public MongoDbLoanApplicationSnapshotRepository(SpringDataMongodeviceStatusEventRepository deviceStatusEventRepository) {
        this.deviceStatusEventRepository = deviceStatusEventRepository;
    }

    @Override
    public Optional<DeviceStatusEvent> retrieveDeviceStatusEvent(Long deviceId){
        List<DeviceStatusEvent> deviceStatusEvents = deviceStatusEventRepository.findByDeviceId(deviceId);
        return deviceStatusEvents.stream().findFirst();
    }

    @Override
    public DeviceStatusEvent createDeviceStatusEvent(DeviceStatusEvent snapshot){
        return deviceStatusEventRepository.save(snapshot);
    }


}