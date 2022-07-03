package com.devicestatus.service;

import com.devicestatus.notify.NotifyDeviceStatusEndpointProvider;


public class TestNotifyDeviceStatusEndpointProvider implements NotifyDeviceStatusEndpointProvider {
    private final String url;

    public TestNotifyDeviceStatusEndpointProvider(String url) {
        this.url = url;
    }

    @Override
    public String notifyUrl(long deviceId) {
        return url;
    }
}
