package com.devicestatus.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "devices")
@Getter
@Setter
@NoArgsConstructor

public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name="name", unique=true)
    private String name;

    @Enumerated(EnumType.STRING)
    @NotNull
    private DeviceStatus status;

    public Device(String name, DeviceStatus status) {
        this.name = name;
        this.status = status;
    }

}
