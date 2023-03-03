package com.bocd.mkt.gatewaysentinelsample.config;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.bocd.mkt.gatewaysentinelsample.constant.EncryptRespFieldEnum;
import com.bocd.mkt.gatewaysentinelsample.constant.ReturnCodeEnum;
import com.bocd.mkt.gatewaysentinelsample.error.GatewayGlobalErrorAttributes;
import com.bocd.mkt.gatewaysentinelsample.error.GatewayGlobalErrorExceptionHandler;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.stream.Collectors;

/**
 * class GatewayGlobalErrorHandlerConfig: 全局错误配置类
 *
 * @author : liuyaodong
 * @date 2023/3/3
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnClass(WebFluxConfigurer.class)
@AutoConfigureBefore(WebFluxAutoConfiguration.class)
@EnableConfigurationProperties({ ServerProperties.class, WebProperties.class })
public class GatewayGlobalErrorHandlerConfig implements InitializingBean {

    private static final String RETURN_CODE = EncryptRespFieldEnum.return_code.name();
    private static final String RETURN_MSG = EncryptRespFieldEnum.return_msg.name();

    private final ServerProperties serverProperties;

    public GatewayGlobalErrorHandlerConfig(ServerProperties serverProperties) {
        this.serverProperties = serverProperties;
    }

    @Bean
    @Order(-1)
    public ErrorWebExceptionHandler errorWebExceptionHandler(ErrorAttributes errorAttributes,
                                                             WebProperties webProperties, ObjectProvider<ViewResolver> viewResolvers,
                                                             ServerCodecConfigurer serverCodecConfigurer, ApplicationContext applicationContext) {
        GatewayGlobalErrorExceptionHandler exceptionHandler = new GatewayGlobalErrorExceptionHandler(errorAttributes,
                webProperties.getResources(), this.serverProperties.getError(), applicationContext);
        exceptionHandler.setViewResolvers(viewResolvers.orderedStream().collect(Collectors.toList()));
        exceptionHandler.setMessageWriters(serverCodecConfigurer.getWriters());
        exceptionHandler.setMessageReaders(serverCodecConfigurer.getReaders());
        return exceptionHandler;
    }

    @Bean
    public ErrorAttributes errorAttributes(){
        return new GatewayGlobalErrorAttributes();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        BlockRequestHandler blockRequestHandler = new BlockRequestHandler() {
            @Override
            public Mono<ServerResponse> handleRequest(ServerWebExchange exchange, Throwable ex) {
                String exceptionClassName = ex.getClass().getName();
                LinkedHashMap<String, String> map = new LinkedHashMap<>();
                map.put(RETURN_CODE, ReturnCodeEnum.FAIL.getValue());
                HttpStatus status;
                if(exceptionClassName.equals("com.alibaba.csp.sentinel.slots.block.degrade.DegradeException")){
                    status = HttpStatus.SERVICE_UNAVAILABLE;
                    map.put(RETURN_MSG, "服务降级！");
                }else if(exceptionClassName.equals("com.alibaba.csp.sentinel.slots.block.flow.FlowException")||
                        exceptionClassName.equals("com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException")){
                    status = HttpStatus.TOO_MANY_REQUESTS;
                    map.put(RETURN_MSG, "访问过快！");
                } else{
                    status = HttpStatus.INTERNAL_SERVER_ERROR;
                    map.put(RETURN_MSG, ex.getMessage());
                }

                return ServerResponse.status(status)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(map));
            }
        };

        GatewayCallbackManager.setBlockHandler(blockRequestHandler);
    }
}
