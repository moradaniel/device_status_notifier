package com.devicestatus.config;

import com.devicestatus.notify.NotifyDeviceStatusEndpointProvider;
import com.devicestatus.notify.NotifyDeviceStatusEndpointProviderImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotifyDeviceStatusEndpointProviderConfig {

    @Value("${partners.notifyStatus.baseUrl}")
    private String notifyBaseUrl;

    @Value("${partners.notifyStatus.urlPath}")
    private String notifyUrlPath;



    @Bean(name = "notifyDeviceStatusEndpointProvider")
    public NotifyDeviceStatusEndpointProvider notifyDeviceStatusEndpointProvider() {
        return new NotifyDeviceStatusEndpointProviderImpl(notifyBaseUrl, notifyUrlPath);
    }



}
