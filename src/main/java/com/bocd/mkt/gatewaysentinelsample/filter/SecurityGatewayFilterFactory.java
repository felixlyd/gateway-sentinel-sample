package com.bocd.mkt.gatewaysentinelsample.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.List;

/**
 * 安全-网关过滤器
 *
 * @author : liuyaodong
 * @date 2023/3/2
 */
@Component
@Slf4j
public class SecurityGatewayFilterFactory extends AbstractGatewayFilterFactory<SecurityGatewayFilterFactory.Config> {

    private static final String ENABLED = "enabled";

    public SecurityGatewayFilterFactory(){
        super(Config.class);
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Collections.singletonList(ENABLED);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            if(!config.isEnabled()){
                return chain.filter(exchange);
            }

            return chain.filter(exchange);
        });
    }

    public static class Config {

        /**
         * 控制是否开启
         */
        private boolean enabled;

        public Config() {}

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
