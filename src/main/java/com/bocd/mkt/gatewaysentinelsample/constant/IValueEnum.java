package com.bocd.mkt.gatewaysentinelsample.constant;

import java.util.HashMap;

public interface IValueEnum {

    /**
     * @return 获取枚举值
     */
    String getValue();

    /**
     * @return 获取枚举描述
     */
    String getMsg();

    /**
     * @return 获取枚举名称对应的枚举值
     */
    HashMap<String, String> getMaps();
}
