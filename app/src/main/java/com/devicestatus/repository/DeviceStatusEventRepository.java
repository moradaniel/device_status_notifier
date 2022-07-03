package com.devicestatus.repository;

import com.devicestatus.domain.DeviceStatusEvent;
import java.util.Optional;

public interface DeviceStatusEventRepository {

    Optional<DeviceStatusEvent> retrieveDeviceStatusEvent(Long deviceId);

    DeviceStatusEvent createDeviceStatusEvent(DeviceStatusEvent snapshot);

}
