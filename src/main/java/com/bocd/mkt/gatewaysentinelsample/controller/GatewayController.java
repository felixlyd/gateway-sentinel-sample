package com.bocd.mkt.gatewaysentinelsample.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 网关一般控制器
 *
 * @author : liuyaodong
 * @date 2023/2/27
 */
@RestController
public class GatewayController {

    /**
     * @return 404页面
     */
    @GetMapping("/404")
    public ResponseEntity<String> pageNotFound(){
        return new ResponseEntity<>("404", HttpStatus.NOT_FOUND);
    }

    @GetMapping("/deny-get-method")
    public ResponseEntity<String> denyGetMethod(){
        return new ResponseEntity<>("拒绝GET请求", HttpStatus.BAD_REQUEST);
    }
}
