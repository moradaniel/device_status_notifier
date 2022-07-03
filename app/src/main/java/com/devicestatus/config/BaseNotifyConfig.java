package com.devicestatus.config;


import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

@Configuration
public class BaseNotifyConfig {

    @Value("${partners.notifyStatus.connectTimeoutMilliseconds}")
    protected Integer connectTimeoutMilliseconds;
    @Value("${partners.notifyStatus.readTimeoutMilliseconds}")
    protected Integer readTimeoutMilliseconds;
    @Value("${partners.notifyStatus.writeTimeoutMilliseconds}")
    protected Integer writeTimeoutMilliseconds;


    @Bean(name = "notifyEndpointRestTemplate")
    public RestTemplate okhttp3Template(/*@Qualifier("keyManager") X509KeyManager keyManager*/) throws Exception{

       // SSLFactory sslFactory = SSLFactory.builder().withIdentityMaterial(keyManager).withDefaultTrustMaterial().build();

        RestTemplate restTemplate = new RestTemplate();

        // create the okhttp client builder
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        //ConnectionPool okHttpConnectionPool = new ConnectionPool(50, 30, TimeUnit.SECONDS);
        //builder.connectionPool(okHttpConnectionPool);
        builder.connectTimeout(connectTimeoutMilliseconds, TimeUnit.MILLISECONDS);
        builder.readTimeout(readTimeoutMilliseconds, TimeUnit.MILLISECONDS);
        builder.writeTimeout(writeTimeoutMilliseconds, TimeUnit.MILLISECONDS);
        //see retry interceptor
        builder.retryOnConnectionFailure(false);

        //builder.sslSocketFactory(sslFactory.getSslSocketFactory(), sslFactory.getTrustManager().orElseThrow());


        //builder.readTimeout(10, TimeUnit.SECONDS);
        //builder.connectTimeout(5, TimeUnit.SECONDS);

        //if (BuildConfig.DEBUG) {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);//BASIC);
        builder.addInterceptor(interceptor);
        //}*/


        // enable metrics in okhttp client
        /*builder.eventListener(OkHttpMetricsEventListener
                .builder(metricRegistry(), "okhttp3.monitor")
                .build());*/
        // embed the created okhttp client to a spring rest template
        restTemplate.setRequestFactory(new OkHttp3ClientHttpRequestFactory(builder.build()));

        return restTemplate;
    }


}
