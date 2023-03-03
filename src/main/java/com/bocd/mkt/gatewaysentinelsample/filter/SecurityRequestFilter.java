package com.bocd.mkt.gatewaysentinelsample.filter;

import cn.hutool.core.util.StrUtil;
import com.bocd.mkt.gatewaysentinelsample.constant.EncryptReqFieldEnum;
import com.bocd.mkt.gatewaysentinelsample.constant.ReqFieldEnum;
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

    private static final String SIGN = EncryptReqFieldEnum.sign.name();
    private static final String DATA = EncryptReqFieldEnum.data.name();

    private static final String TIMESTAMP = ReqFieldEnum.timestamp.name();
    private static final String NONCE = ReqFieldEnum.nonce.name();

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private SmService smService;
    @Autowired
    private DataReplayDefenseService dataReplayDefenseService;

    /**
     * 处理请求数据
     *
     * @param serverWebExchange 请求上下文
     * @param encryptRequest           请求数据
     * @return 处理后的请求数据
     */
    @Override
    public Publisher<String> apply(ServerWebExchange serverWebExchange, String encryptRequest) {

        // 1. 请求数据为空，直接报错
        if(StrUtil.isEmpty(encryptRequest)){
            throw new RuntimeException("非法请求！请求数据不能为空！");
        }

        // 2. 获取请求数据
        // 2.1. 转换为map
        HashMap<String, String > encryptReqMap;
        try {
           encryptReqMap = objectMapper.readValue(encryptRequest, new TypeReference<HashMap<String , String >>() {});
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(),e.fillInStackTrace());
            throw new RuntimeException("反序列化失败！");
        }
        // 2.2. 空值判断校验
        if(isKeyExistsAndNotEmpty(encryptReqMap, SIGN)){
            throw new RuntimeException("请求报文中缺少"+ SIGN +"或值为空！");
        }
        if(isKeyExistsAndNotEmpty(encryptReqMap, DATA)){
            throw new RuntimeException("请求报文中缺少"+ DATA +"或值为空！");
        }
        // 2.3. 获取签名和请求数据
        String sign = encryptReqMap.get(SIGN);
        String data = encryptReqMap.get(DATA);
        // 2.4. 解密请求数据，获得请求数据json字符串
        log.info("解密--");
        String jsonReq = smService.sm4Decrypt(data);

        // 3. 数据签名验证
        log.info("签名验证--");
        String sm3data = smService.sm3Encrypt(jsonReq);
        boolean isSignLegal = smService.verifySign(sm3data, sign);
        if(!isSignLegal){
            throw new RuntimeException("数据签名不通过，数据不合法！");
        }

        // 4. 请求数据反序列化成map对象
        HashMap<String, Object> requestMap;
        try {
            requestMap = objectMapper.readValue(jsonReq, new TypeReference<HashMap<String , Object>>() {});
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(),e.fillInStackTrace());
            throw new RuntimeException("反序列化失败！");
        }

        // 5. 数据防重放验证
        log.info("数据防重放验证--");
        if(requestMap.containsKey(TIMESTAMP)&&requestMap.containsKey(NONCE)){
            Long timestamp = Long.valueOf(String.valueOf(requestMap.get(TIMESTAMP)));
            String nonce = String.valueOf(requestMap.get(NONCE));
            boolean isLegal = dataReplayDefenseService.isNotDataReplay(timestamp, nonce);
            if(!isLegal){
                throw new RuntimeException("该数据已经处理过，请勿提交重复数据！");
            }
        }else {
            throw new RuntimeException("请求报文中缺少"+ TIMESTAMP +"和"+ NONCE +"!");
        }

        // 6. 转换为字符串
        String decryptRequest;
        try {
            decryptRequest = objectMapper.writeValueAsString(requestMap);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return Mono.just(decryptRequest);
    }

    private boolean isKeyExistsAndNotEmpty(Map<String, String> map, String key){
        if(map.containsKey(key)){
            String value = String.valueOf(map.get(key));
            return value == null || value.length() == 0;
        }
        return true;
    }
}
