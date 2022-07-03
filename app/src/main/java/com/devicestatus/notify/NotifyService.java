package com.devicestatus.notify;

import com.devicestatus.notify.payload.request.DeviceStatusNotifyRequest;
import com.devicestatus.notify.payload.response.DeviceStatusNotifyResponse;
import com.devicestatus.exception.PartnerServiceNotAvailableException;

public interface NotifyService {
    DeviceStatusNotifyResponse notify(DeviceStatusNotifyRequest deviceStatusNotifyRequest)throws PartnerServiceNotAvailableException;
}
