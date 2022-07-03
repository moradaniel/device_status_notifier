package com.devicestatus.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@EqualsAndHashCode
@Document
@Builder
@TypeAlias("DeviceStatusEvent")
public class DeviceStatusEvent {

    @Id
    @NotNull
    private Long deviceId;

    @NotBlank
    private DeviceStatus status;

    public static DeviceStatusEvent of(DeviceStatusEventAvro record) {
        return new DeviceStatusEvent(Long.valueOf(record.getDeviceId()), DeviceStatus.valueOf(record.getStatus()));
    }

    public static DeviceStatusEventAvro toAvro(DeviceStatusEvent deviceStatusEvent) {
        return new DeviceStatusEventAvro(String.valueOf(deviceStatusEvent.getDeviceId()), deviceStatusEvent.getStatus().name());
    }


}
