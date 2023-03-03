package com.bocd.mkt.gatewaysentinelsample.filter;

import cn.hutool.core.util.StrUtil;
import com.bocd.mkt.gatewaysentinelsample.constant.EncryptRespFieldEnum;
import com.bocd.mkt.gatewaysentinelsample.constant.ReturnCodeEnum;
import com.bocd.mkt.gatewaysentinelsample.service.security.SmService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;

@Component
@Slf4j
public class SecurityResponseFilter implements RewriteFunction<String, String> {

    private static final String DATA= EncryptRespFieldEnum.data.name();
    private static final String RETURN_CODE = EncryptRespFieldEnum.return_code.name();
    private static final String RETURN_MSG = EncryptRespFieldEnum.return_msg.name();

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SmService smService;

    /**
     * 处理请求数据
     *
     * @param serverWebExchange 请求上下文
     * @param decryptResponse   响应数据
     * @return 处理后的响应数据
     */
    @Override
    public Publisher<String> apply(ServerWebExchange serverWebExchange, String decryptResponse) {
        // 1. 准备加密响应map
        HashMap<String , String > encryptRespMap = new HashMap<>();

        // 2. 响应为空，响应map中设值为失败，加密后装入加密响应map的data字段中
        if(StrUtil.isEmpty(decryptResponse)){
            encryptRespMap.put(DATA,"");
            encryptRespMap.put(RETURN_MSG,"原始响应数据为空！");
            encryptRespMap.put(RETURN_CODE, ReturnCodeEnum.FAIL.getValue());
        }

        // 3. 响应数据不为空，处理响应数据
        String encrypt = smService.sm4Encrypt(decryptResponse);

        encryptRespMap.put(DATA, encrypt);
        encryptRespMap.put(RETURN_MSG, "");
        encryptRespMap.put(RETURN_CODE, ReturnCodeEnum.SUCCESS.getValue());

        // 4. 序列化加密响应map，并返回
        try {
            String encryptResponse = objectMapper.writeValueAsString(encryptRespMap);
            log.info(encryptResponse);
            return Mono.just(encryptResponse);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e.fillInStackTrace());
            throw new RuntimeException("序列化响应报文失败，请查看日志！");
        }
    }
}
