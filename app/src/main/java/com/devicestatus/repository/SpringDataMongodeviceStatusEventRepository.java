package com.devicestatus.repository;


import com.devicestatus.domain.DeviceStatusEvent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataMongodeviceStatusEventRepository extends MongoRepository<DeviceStatusEvent, Long> {

    List<DeviceStatusEvent> findByDeviceId(Long deviceId);
}
