package com.bocd.mkt.gatewaysentinelsample.config;

import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.digest.SM3;
import cn.hutool.crypto.symmetric.SM4;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 国密管理器配置
 *
 * @author : liuyaodong
 * @date 2023/2/10
 */
@Configuration
public class SmConfig {
    @Value("${sm.sm4key}")
    private String sm4Key;

    @Bean("sm4")
    public SM4 sm4(){
        byte[] sm4KeyBytes = Base64.getDecoder().decode(sm4Key.getBytes(StandardCharsets.UTF_8));
        return new SM4(Mode.CFB, Padding.NoPadding, sm4KeyBytes, sm4KeyBytes);
    }

    @Bean("sm3")
    public SM3 sm3(){
        return new SM3();
    }
}
