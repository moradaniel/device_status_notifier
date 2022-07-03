package com.devicestatus.service;

import com.devicestatus.domain.DeviceStatusEvent;

public interface DeviceStatusService {

    void process(DeviceStatusEvent deviceStatusEvent)throws Exception;
}
