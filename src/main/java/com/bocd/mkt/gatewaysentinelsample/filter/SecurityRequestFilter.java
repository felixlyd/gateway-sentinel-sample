package com.bocd.mkt.gatewaysentinelsample.filter;

import cn.hutool.core.util.StrUtil;
import com.bocd.mkt.gatewaysentinelsample.service.security.DataReplayDefenseService;
import com.bocd.mkt.gatewaysentinelsample.service.security.SmService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class SecurityRequestFilter implements RewriteFunction<String, String> {

    private static final String SIGN_KEY="sign";
    private static final String DATA_KEY="data";
    private static final String TIMESTAMP_KEY="timestamp";
    private static final String NONCE_KEY="nonce";

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private SmService smService;
    @Autowired
    private DataReplayDefenseService dataReplayDefenseService;

    @Override
    public Publisher<String> apply(ServerWebExchange serverWebExchange, String request) {

        // 1. 请求数据为空，直接报错
        if(StrUtil.isEmpty(request)){
            throw new RuntimeException("非法请求！请求数据不能为空！");
        }

        // 2. 获取请求数据
        // 2.1. 转换为map
        HashMap<String, String > reqEncryptMap;
        try {
           reqEncryptMap = objectMapper.readValue(request, new TypeReference<HashMap<String , String >>() {});
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(),e.fillInStackTrace());
            throw new RuntimeException("反序列化失败！");
        }
        // 2.2. 空值判断校验
        if(isKeyExistsAndNotEmpty(reqEncryptMap, SIGN_KEY)){
            throw new RuntimeException("请求报文中缺少"+SIGN_KEY+"或值为空！");
        }
        if(isKeyExistsAndNotEmpty(reqEncryptMap, DATA_KEY)){
            throw new RuntimeException("请求报文中缺少"+DATA_KEY+"或值为空！");
        }
        // 2.3. 获取签名和请求数据
        String sign = reqEncryptMap.get(SIGN_KEY);
        String data = reqEncryptMap.get(DATA_KEY);
        // 2.4. 解密请求数据，获得请求数据json字符串
        log.info("解密--");
        String reqJson = smService.sm4Decrypt(data);

        // 3. 数据签名验证
        log.info("签名验证--");
        String reqSign = smService.sm3Encrypt(reqJson);
        boolean isSignLegal = smService.verifySign(reqSign, sign);
        if(!isSignLegal){
            throw new RuntimeException("数据签名不通过，数据不合法！");
        }

        // 4. 请求数据反序列化成map对象
        HashMap<String, Object> requestMap;
        try {
            requestMap = objectMapper.readValue(reqJson, new TypeReference<HashMap<String , Object>>() {});
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(),e.fillInStackTrace());
            throw new RuntimeException("反序列化失败！");
        }

        // 5. 数据防重放验证
        log.info("数据防重放验证--");
        if(requestMap.containsKey(TIMESTAMP_KEY)&&requestMap.containsKey(NONCE_KEY)){
            Long timestamp = Long.valueOf(String.valueOf(requestMap.get(TIMESTAMP_KEY)));
            String nonce = String.valueOf(requestMap.get(NONCE_KEY));
            boolean isLegal = dataReplayDefenseService.isNotDataReplay(timestamp, nonce);
            if(!isLegal){
                throw new RuntimeException("该数据已经处理过，请勿提交重复数据！");
            }
        }else {
            throw new RuntimeException("请求报文中缺少"+TIMESTAMP_KEY+"和"+NONCE_KEY+"!");
        }

        // 6. 转换为字符串
        String originRequest;
        try {
            originRequest = objectMapper.writeValueAsString(requestMap);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return Mono.just(originRequest);
    }

    private boolean isKeyExistsAndNotEmpty(Map<String, String> map, String key){
        if(map.containsKey(key)){
            String value = String.valueOf(map.get(key));
            return value == null || value.length() == 0;
        }
        return true;
    }
}
