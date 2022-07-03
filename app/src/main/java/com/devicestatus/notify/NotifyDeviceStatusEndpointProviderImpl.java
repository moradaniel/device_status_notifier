package com.devicestatus.notify;

import io.vavr.Function2;
import io.vavr.Function3;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;


@Slf4j
public class NotifyDeviceStatusEndpointProviderImpl implements NotifyDeviceStatusEndpointProvider {

    private String notifyBaseUrl;
    private String notifyUrlPath;

    @Autowired
    public NotifyDeviceStatusEndpointProviderImpl(@Value("${notify.baseUrl}") String notifyBaseUrl,
                                                  @Value("${notify.urlPath}") String notifyUrlPath) {
        this.notifyBaseUrl = notifyBaseUrl;
        this.notifyUrlPath = notifyUrlPath;
    }

    @Override
    public String notifyUrl(long deviceId) {
        return notifyBaseUrl + notifyUrlPath+"/"+deviceId;
    }

}
