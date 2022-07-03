package com.devicestatus.service.impl;


import com.devicestatus.domain.Device;
import com.devicestatus.repository.DeviceRepository;
import com.devicestatus.service.DeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {


    private final DeviceRepository deviceRepository;

    @Transactional
    @Override
    public Device save(Device device) {
        return deviceRepository.save(device);
    }

    @Override
    public Optional<Device> findByDeviceId(long deviceId) {
        return deviceRepository.findById(deviceId);
    }


}
