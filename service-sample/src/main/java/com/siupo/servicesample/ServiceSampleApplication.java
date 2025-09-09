package com.siupo.servicesample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ServiceSampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceSampleApplication.class, args);
    }

}
