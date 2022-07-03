package com.devicestatus.service;


import com.devicestatus.domain.Device;

import java.util.Optional;

public interface DeviceService {

    Device save(Device device);
    Optional<Device> findByDeviceId(long deviceId);
}
