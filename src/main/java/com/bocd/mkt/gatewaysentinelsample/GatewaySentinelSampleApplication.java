package com.bocd.mkt.gatewaysentinelsample;

import com.alibaba.cloud.sentinel.SentinelProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(SentinelProperties.class)
@ConfigurationPropertiesScan
public class GatewaySentinelSampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewaySentinelSampleApplication.class, args);
    }

}
