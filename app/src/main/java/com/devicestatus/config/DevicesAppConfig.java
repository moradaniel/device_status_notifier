package com.devicestatus.config;

import com.devicestatus.notify.NotifyDeviceStatusEndpointProvider;
import com.devicestatus.notify.NotifyService;
import com.devicestatus.notify.NotifyServiceImpl;
import com.devicestatus.repository.DeviceStatusEventRepository;
import com.devicestatus.repository.MongoDbLoanApplicationSnapshotRepository;
import com.devicestatus.repository.SpringDataMongodeviceStatusEventRepository;
import io.github.resilience4j.retry.RetryConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class DevicesAppConfig {

    @Bean(name = "notifyService")
    public NotifyService notifyService(@Qualifier("notifyEndpointRestTemplate") RestTemplate restTemplate,
                                       @Qualifier("notifyDeviceStatusEndpointProvider") NotifyDeviceStatusEndpointProvider notifyDeviceStatusEndpointProvider,
                                       @Qualifier("notifyEndpointRetryConfig") RetryConfig notifyEndpointRetryConfig
    ) {
        return new NotifyServiceImpl(restTemplate,notifyDeviceStatusEndpointProvider,notifyEndpointRetryConfig);
    }

    @Bean(name = "deviceStatusEventRepository")
    public DeviceStatusEventRepository deviceStatusEventRepository(@Autowired SpringDataMongodeviceStatusEventRepository springDataMongoLoanApplicationSnapshotRepository) {
        return new MongoDbLoanApplicationSnapshotRepository(springDataMongoLoanApplicationSnapshotRepository);
    }

}
