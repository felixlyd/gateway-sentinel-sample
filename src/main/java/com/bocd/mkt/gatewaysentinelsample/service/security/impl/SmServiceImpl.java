package com.bocd.mkt.gatewaysentinelsample.service.security.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.SM3;
import cn.hutool.crypto.symmetric.SM4;
import com.bocd.mkt.gatewaysentinelsample.service.security.SmService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.TreeMap;

/**
 * 加密方法实现类
 *
 * @author : liuyaodong
 * @date 2023/2/10
 */
@Service
public class SmServiceImpl implements SmService {

    @Autowired
    @Qualifier("sm4")
    private SM4 sm4;

    @Autowired
    @Qualifier("sm3")
    private SM3 sm3;

    @Autowired
    private ObjectMapper objectMapper;


    /**
     * sm4算法加密 编码基于Base64
     *
     * @param originStr 加密的字符串
     * @return 解密的字符串
     */
    @Override
    public String sm4Encrypt(String originStr) {
        // 1. 字符串转字节码，字节码经由SM4加密
        byte[] encryptBytes = sm4.encrypt(originStr.getBytes(StandardCharsets.UTF_8));
        // 2. 字节码由Base64编码，通过UTF-8字符集转为字符串
        return new String(Base64.getEncoder().encode(encryptBytes), StandardCharsets.UTF_8);
    }

    /**
     * sm4算法解密 需Base64解码
     * @param encryptStr 加密的字符串
     * @return 解密的字符串
     */
    @Override
    public String sm4Decrypt(String encryptStr) {
        // 1. 字符串转字节码，字节码经Base64解码还原为加密字节码
        byte[] encryptBytes = Base64.getDecoder().decode(encryptStr.getBytes(StandardCharsets.UTF_8));
        // 2. 经由SM4解密为原始字节码
        byte[] originBytes = sm4.decrypt(encryptBytes);
        // 3. 原始字节码直接通过UTF-8字符集转为字符串
        return new String(originBytes, StandardCharsets.UTF_8);
    }

    /**
     * sm4算法加密 编码基于Base64(urlSafe)
     *
     * @param originStr 加密的字符串
     * @return 解密的字符串
     */
    @Override
    public String sm4EncryptUrlSafe(String originStr) {
        // 1. 字符串转字节码，字节码经由SM4加密
        byte[] encryptBytes = sm4.encrypt(originStr.getBytes(StandardCharsets.UTF_8));
        // 2. 字节码由Base64编码(Base64UrlSafe版本)，通过UTF-8字符集转为字符串
        return new String(Base64.getUrlEncoder().encode(encryptBytes), StandardCharsets.UTF_8);
    }

    /**
     * sm4算法解密 需Base64解码(urlSafe)
     * @param encryptStr 加密的字符串
     * @return 解密的字符串
     */
    @Override
    public String sm4DecryptUrlSafe(String encryptStr) {
        // 1. 字符串转字节码，字节码经Base64解码(Base64UrlSafe版本)还原为加密字节码
        byte[] encryptBytes = Base64.getUrlDecoder().decode(encryptStr.getBytes(StandardCharsets.UTF_8));
        // 2. 经由SM4解密为原始字节码
        byte[] originBytes = sm4.decrypt(encryptBytes);
        // 3. 原始字节码直接通过UTF-8字符集转为字符串
        return new String(originBytes, StandardCharsets.UTF_8);
    }


    /**
     * 验签
     *
     * @param request 请求报文
     * @param sign    签名
     * @return 是否验证通过
     */
    @Override
    public boolean verifySign(String request, String sign) {
        return StrUtil.equals(request, sign);
    }

    /**
     * 验签
     *
     * @param requestMap 请求报文
     * @param sign       签名
     * @return 是否验证通过
     */
    @Override
    public boolean verifySign(HashMap<String, Object> requestMap, String sign) {
        // 1. 原始报文通过排序、处理成字符串、SM3加密得到摘要
        String digest = sm3Encrypt(requestMap);
        // 2. 比对上送签名和加密摘要是否相等
        return verifySign(digest, sign);
    }


    /**
     * 验签
     *
     * @param requestMap 请求报文
     * @param sign       签名
     * @return 是否验证通过
     */
    @Override
    public boolean verifySign(TreeMap<String, Object> requestMap, String sign) {
        // 1. 原始报文通过排序、处理成字符串、SM3加密得到摘要
        String digest = sm3Encrypt(requestMap);
        // 2. 比对上送签名和加密摘要是否相等
        return verifySign(digest, sign);
    }

    /**
     * sm3摘要加密 编码基于Base64
     *
     * @param originStr 原始字符串
     * @return 摘要
     */
    @Override
    public String sm3Encrypt(String originStr) {
        // 1. 字符串转字节码，字节码经由SM3加密
        byte[] encryptBytes = sm3.digest(originStr.getBytes(StandardCharsets.UTF_8));
        // 2. 字节码由Base64编码，通过UTF-8字符集转为字符串
        return new String(Base64.getEncoder().encode(encryptBytes), StandardCharsets.UTF_8);
    }


    /**
     * sm3摘要加密 编码基于Base64
     *
     * @param requestMap 请求报文
     * @return 摘要
     */
    @Override
    public String sm3Encrypt(HashMap<String, Object> requestMap) {
        // 1. 原始报文排序、处理成字符串
        String originStr = handleReqMap(requestMap);
        // 2. 经由SM3加密
        return sm3Encrypt(originStr);
    }


    /**
     * sm3摘要加密 编码基于Base64
     *
     * @param requestMap 请求报文
     * @return 摘要
     */
    @Override
    public String sm3Encrypt(TreeMap<String, Object> requestMap) {
        // 1. 原始报文排序、处理成字符串
        String originStr = handleReqMap(requestMap);
        // 2. 经由SM3加密
        return sm3Encrypt(originStr);
    }


    /**
     * 处理请求报文，转换为字符串
     *
     * @param requestMap 请求报文
     * @return 字符串
     */
    @Override
    public String handleReqMap(HashMap<String, Object> requestMap) {
        // treemap基于ASCLL编码排序
        TreeMap<String ,Object> treeMap = new TreeMap<>(requestMap);
        return handleReqMap(treeMap);
    }




    /**
     * 处理请求报文，转换为字符串
     *
     * @param requestMap 请求报文
     * @return 字符串
     */
    @Override
    public String handleReqMap(TreeMap<String, Object> requestMap) {
        try {
            return objectMapper.writeValueAsString(requestMap);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
