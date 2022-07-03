package com.devicestatus.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@EqualsAndHashCode
public class DeviceStatusChange {

    @NotNull
    private Long deviceId;

    private DeviceStatus previousStatus;

    @NotBlank
    private DeviceStatus newStatus;


    public boolean isNotifiable() {
        return
           (previousStatus == DeviceStatus.up && newStatus==DeviceStatus.alerted) ||
           (previousStatus == DeviceStatus.alerted && newStatus==DeviceStatus.up) ||
           (previousStatus == DeviceStatus.alerted && newStatus==DeviceStatus.down) ||
           (previousStatus == DeviceStatus.down && newStatus==DeviceStatus.alerted);
    }
}
