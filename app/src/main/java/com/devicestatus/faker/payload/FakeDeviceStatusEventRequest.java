package com.devicestatus.faker.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class FakeDeviceStatusEventRequest {


    @JsonProperty("device_id")
    @NotNull
    @Schema(description = "Device Id",
            example = "120621744", required = true)
    private Long deviceId;


    //"status":string //it might be down, up or alerted
    @JsonProperty("status")
    @NotEmpty
    private String status;

}
