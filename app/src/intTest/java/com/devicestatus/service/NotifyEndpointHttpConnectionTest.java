package com.devicestatus.service;

import com.devicestatus.config.BaseNotifyConfig;
import com.devicestatus.config.NotifyEndpointRetryStrategyConfig;
import com.devicestatus.domain.DeviceStatus;
import com.devicestatus.exception.PartnerServiceNotAvailableException;
import com.devicestatus.notify.NotifyService;
import com.devicestatus.notify.NotifyServiceImpl;
import com.devicestatus.notify.payload.request.DeviceStatusNotifyRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.retry.RetryConfig;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockWebServer;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

@ExtendWith(SpringExtension.class)
@JsonTest
@Import({})
@Testcontainers
@ContextConfiguration(classes = {NotifyEndpointRetryStrategyConfig.class,
        BaseNotifyConfig.class
})
@Profile("intTest")
public class NotifyEndpointHttpConnectionTest {

    @Container
    public static final DockerComposeContainer COMPOSE_CONTAINER_HTTPBIN = new DockerComposeContainer(
            new File("src/intTest/resources/docker/docker-compose-httpbin-test.yml"))
            .withLocalCompose(true);

    static {
        COMPOSE_CONTAINER_HTTPBIN.start();
    }



    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    @Qualifier("notifyEndpointRestTemplate")
    RestTemplate restTemplate;


    @Autowired
    @Qualifier("notifyEndpointRetryConfig")
    RetryConfig notifyEndpointRetryConfig;

    public MockWebServer mockWebServer;

    String BASE_URL = "";

    String notifyUrlPath = "/devices";


    @BeforeEach
    public void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String scorePath = "/test";

        // Ask the server for its URL. You'll need this to make HTTP requests.
        HttpUrl notifyUrl = mockWebServer.url(scorePath);


    }

    @AfterEach
    public void tearDown() throws IOException {
        mockWebServer.shutdown();
    }


    @Test
    @DisplayName("Test notify endpoint Read timed out")
    public void test_notify_endpoint_read_timeout() throws Exception {
        BASE_URL = "http://localhost:81";
        notifyUrlPath = "/delay/16"; //delay 16 seconds


        TestNotifyDeviceStatusEndpointProvider notifyDeviceStatusEndpointProvider = new TestNotifyDeviceStatusEndpointProvider(BASE_URL+notifyUrlPath);
        NotifyService notifyService = new NotifyServiceImpl(restTemplate, notifyDeviceStatusEndpointProvider,notifyEndpointRetryConfig);


        try {
            notifyService.notify(new DeviceStatusNotifyRequest(1l, DeviceStatus.alerted));
            fail("Read timed out SocketException should be thrown");
        } catch (PartnerServiceNotAvailableException ex) {
            Throwable rootCause = ExceptionUtils.getRootCause(ex);
            //cause: java.net.SocketTimeoutException: Read timed out

            assertThat((rootCause instanceof SocketTimeoutException) || (rootCause instanceof SocketException)).isTrue();
            assertThat( rootCause.getMessage().contains("Read timed out") || rootCause.getMessage().contains("Socket closed")).isTrue();
        }

    }

    @Test
    @DisplayName("Test notify endpoint connect timed out")
    public void test_notify_connect_timeout_exception() throws Exception {

        // non routable address
        String nonRoutableAddress = "http://203.0.113.1";

        BASE_URL = nonRoutableAddress;

        notifyUrlPath = "";

        TestNotifyDeviceStatusEndpointProvider notifyDeviceStatusEndpointProvider = new TestNotifyDeviceStatusEndpointProvider(BASE_URL+notifyUrlPath);
        NotifyService notifyService = new NotifyServiceImpl(restTemplate, notifyDeviceStatusEndpointProvider,notifyEndpointRetryConfig);

        try {
            notifyService.notify(new DeviceStatusNotifyRequest(1l, DeviceStatus.alerted));
            fail("Connect timeout SocketException should be thrown");
        } catch (PartnerServiceNotAvailableException ex) {
            Throwable rootCause = ExceptionUtils.getRootCause(ex);
            //cause: java.net.SocketTimeoutException: Read timed out
            assertThat(rootCause).isInstanceOf(SocketTimeoutException.class);
            assertThat(rootCause.getMessage()).contains("connect timed out");
        }

    }


    @Test
    @DisplayName("Test notify endpoint Connection refused")
    public void test_notify_connection_refused_exception() throws Exception {

        mockWebServer.shutdown();

        URL url = mockWebServer.url("/").url();

        BASE_URL = url.toString();

        notifyUrlPath = "";

        TestNotifyDeviceStatusEndpointProvider notifyDeviceStatusEndpointProvider = new TestNotifyDeviceStatusEndpointProvider(BASE_URL+notifyUrlPath);
        NotifyService notifyService = new NotifyServiceImpl(restTemplate, notifyDeviceStatusEndpointProvider,notifyEndpointRetryConfig);

        try {
            notifyService.notify(new DeviceStatusNotifyRequest(1l, DeviceStatus.alerted));
            fail("Connection refused should be thrown");
        } catch (PartnerServiceNotAvailableException ex) {
            Throwable rootCause = ExceptionUtils.getRootCause(ex);
            //cause: java.net.SocketTimeoutException: Read timed out
            assertThat(rootCause).isInstanceOf(ConnectException.class);
            assertThat(rootCause.getMessage()).contains("Connection refused");
        }
    }

}
