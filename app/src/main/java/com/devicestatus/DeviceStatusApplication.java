package com.devicestatus;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@OpenAPIDefinition
@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
public class DeviceStatusApplication {

	public static void main(String[] args) {
		SpringApplication.run(DeviceStatusApplication.class, args);
	}

}
