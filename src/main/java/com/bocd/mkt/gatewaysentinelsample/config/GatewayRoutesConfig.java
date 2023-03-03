package com.bocd.mkt.gatewaysentinelsample.config;

import com.bocd.mkt.gatewaysentinelsample.filter.SecurityRequestFilter;
import com.bocd.mkt.gatewaysentinelsample.filter.SecurityResponseFilter;
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

    @Autowired
    private SecurityResponseFilter securityResponseFilter;

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder){

        // 配置路由
        return builder.routes().route("test", r -> r
                // 匹配路径
                .path("/api-a/**")
                // 过滤器
                .filters(f -> f
                        // 修改请求数据过滤器
                        .modifyRequestBody(String.class,String.class, MediaType.APPLICATION_JSON_VALUE, securityRequestFilter)
                        // 修改响应数据过滤器
                        .modifyResponseBody(String.class,String.class, MediaType.APPLICATION_JSON_VALUE, securityResponseFilter)
                )
                // 转发uri
                .uri("lb://RULE-LITEFLOW")
        ).build();
    }
}
