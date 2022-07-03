package com.devicestatus.notify.payload.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class DeviceStatusNotifyResponse {

   private String result;

   @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
   public DeviceStatusNotifyResponse(@JsonProperty("result") String result) {
      this.result = result;
   }

}
