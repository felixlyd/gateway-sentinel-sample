package com.bocd.mkt.gatewaysentinelsample.service.security.impl;

import com.bocd.mkt.gatewaysentinelsample.config.SmConfig;
import com.bocd.mkt.gatewaysentinelsample.service.security.SmService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        classes = {SmServiceImpl.class, SmConfig.class, ObjectMapper.class},
        initializers = {ConfigDataApplicationContextInitializer.class}
)
@ActiveProfiles("dev")
@Slf4j
public class SmServiceImplTest {

    @Autowired
    @Qualifier("smServiceImpl")
    private SmService smService;

    @Test
    public void sm4Encrypt() throws JsonProcessingException {
        HashMap<String , String> map = new HashMap<>();
        map.put("nonce","1");
        map.put("timestamp", String.valueOf(System.currentTimeMillis()));
        map.put("ruleIdList","1,2,3");
        map.put("custNo","111");
        map.put("cardNo","111");
        ObjectMapper objectMapper = new ObjectMapper();
        String str = objectMapper.writeValueAsString(map);
        String encryptStr = smService.sm4Encrypt(str);
        System.out.println(encryptStr);
        String sign = smService.sm3Encrypt(str);
        System.out.println(sign);
    }
}