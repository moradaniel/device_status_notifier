package com.devicestatus.notify.payload.request;

import com.devicestatus.domain.DeviceStatus;
import com.devicestatus.domain.DeviceStatusEvent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class DeviceStatusNotifyRequest {

   private long id;
   private DeviceStatus status;

   @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
   public DeviceStatusNotifyRequest(@JsonProperty("id") long id,
                                    @JsonProperty("status") DeviceStatus status) {
      this.id = id;
      this.status = status;
   }

   public static DeviceStatusNotifyRequest of(DeviceStatusEvent deviceStatusEvent) {
            return new DeviceStatusNotifyRequest(deviceStatusEvent.getDeviceId(), deviceStatusEvent.getStatus());
   }
}
