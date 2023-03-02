package com.bocd.mkt.gatewaysentinelsample.config;

import com.bocd.mkt.gatewaysentinelsample.filter.SecurityRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;

@Configuration
public class GatewayRoutesConfig {

    @Autowired
    private SecurityRequestFilter securityRequestFilter;

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder){

        return builder.routes().route("test", r -> r
                        .path("/api-a/**")
                        .filters(f -> f
                                .modifyRequestBody(String.class,String.class, MediaType.APPLICATION_JSON_VALUE, securityRequestFilter))
                        .uri("lb://RULE-LITEFLOW")
        ).build();
    }
}
