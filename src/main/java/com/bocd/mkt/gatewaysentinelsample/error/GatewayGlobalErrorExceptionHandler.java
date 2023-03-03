package com.bocd.mkt.gatewaysentinelsample.error;

import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;

import java.util.Map;

/**
 * class GatewayGlobalErrorExceptionHandler: 自定义异常处理类
 *
 * @author : liuyaodong
 * @date 2023/3/3
 */
public class GatewayGlobalErrorExceptionHandler extends DefaultErrorWebExceptionHandler {

    private static final String STATUS = "status";

    public GatewayGlobalErrorExceptionHandler(ErrorAttributes errorAttributes, WebProperties.Resources resources, ErrorProperties errorProperties, ApplicationContext applicationContext) {
        super(errorAttributes, resources, errorProperties, applicationContext);
    }

    @Override
    protected int getHttpStatus(Map<String, Object> errorAttributes) {
        int status = (int) errorAttributes.get(STATUS);
        errorAttributes.remove(STATUS);
        return status;
    }
}
