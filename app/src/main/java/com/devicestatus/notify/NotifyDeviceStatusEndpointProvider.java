package com.devicestatus.notify;

public interface NotifyDeviceStatusEndpointProvider {
    String notifyUrl(long deviceId);
}
