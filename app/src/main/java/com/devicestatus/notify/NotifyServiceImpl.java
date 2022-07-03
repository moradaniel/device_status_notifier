package com.devicestatus.notify;


import com.devicestatus.exception.PartnerServiceNotAvailableException;
import com.devicestatus.notify.payload.request.DeviceStatusNotifyRequest;
import com.devicestatus.notify.payload.response.DeviceStatusNotifyResponse;
import com.devicestatus.util.Utils;
import io.github.resilience4j.core.registry.EntryAddedEvent;
import io.github.resilience4j.core.registry.EntryRemovedEvent;
import io.github.resilience4j.core.registry.EntryReplacedEvent;
import io.github.resilience4j.core.registry.RegistryEventConsumer;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.vavr.CheckedFunction0;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class NotifyServiceImpl implements NotifyService {

    private final RestTemplate restTemplate;
    private final NotifyDeviceStatusEndpointProvider notifyDeviceStatusEndpointProvider;
    private final RetryConfig notifyEndpointRetryConfig;


    @Autowired
    public NotifyServiceImpl(RestTemplate restTemplate,
                             NotifyDeviceStatusEndpointProvider notifyDeviceStatusEndpointProvider,
                              RetryConfig notifyEndpointRetryConfig) {
       this.restTemplate = restTemplate;
       this.notifyDeviceStatusEndpointProvider = notifyDeviceStatusEndpointProvider;
       this.notifyEndpointRetryConfig = notifyEndpointRetryConfig;
    }


    public DeviceStatusNotifyResponse notify(DeviceStatusNotifyRequest deviceStatusNotifyRequest) throws PartnerServiceNotAvailableException {
        log.info("Attempting to notify DeviceStatus {}", deviceStatusNotifyRequest);

        RetryRegistry registry = RetryRegistry.of(notifyEndpointRetryConfig ,
                new RegistryEventConsumer<Retry>() {
                    @Override
                    public void onEntryAddedEvent(EntryAddedEvent<Retry> entryAddedEvent) {
                        entryAddedEvent.getAddedEntry().getEventPublisher()
                                .onEvent(event -> log.info(event.toString()));
                    }

                    @Override
                    public void onEntryRemovedEvent(EntryRemovedEvent<Retry> entryRemoveEvent) {
                    }

                    @Override
                    public void onEntryReplacedEvent(EntryReplacedEvent<Retry> entryReplacedEvent) {
                    }
                });

        Retry retry = registry.retry("notifyEndpointServiceRetry", notifyEndpointRetryConfig);

        CheckedFunction0<DeviceStatusNotifyResponse> notifyDeviceStatusEndpointCall =
                Retry.decorateCheckedSupplier(retry,
                        () -> callNotifyEndpoint(deviceStatusNotifyRequest));

        DeviceStatusNotifyResponse deviceStatusNotifyResponse = null;
        try {
            deviceStatusNotifyResponse = notifyDeviceStatusEndpointCall.apply();
        } catch (Throwable ex) {
            // handle exception that can occur after retries are exhausted
            throw new PartnerServiceNotAvailableException(ex.getMessage(), ex);
        }

        return deviceStatusNotifyResponse;
    }

    private DeviceStatusNotifyResponse callNotifyEndpoint(DeviceStatusNotifyRequest deviceStatusNotifyRequest) throws Exception{
        try {
            final HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<DeviceStatusNotifyRequest> request = new HttpEntity<>(deviceStatusNotifyRequest, headers);
            var response = restTemplate.postForObject(notifyDeviceStatusEndpointProvider.notifyUrl(deviceStatusNotifyRequest.getId()),
                    request,
                    DeviceStatusNotifyResponse.class);
            return response;

        } catch (Exception ex) {
            String errorMessage = String.format("Error while calling notify api for deviceId:\"%s\".", deviceStatusNotifyRequest.getId());
            log.error(Utils.makeExceptionLogEntry(ex, errorMessage, String.valueOf(deviceStatusNotifyRequest.getId())));
            throw ex;
        }
    }

}
