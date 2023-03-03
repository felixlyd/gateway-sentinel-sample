package com.bocd.mkt.gatewaysentinelsample.constant;

import java.util.HashMap;

public enum ReturnCodeEnum implements IValueEnum {
    SUCCESS("SUCCESS","成功"),
    FAIL("FAIL","失败")
    ;

    private final String value;
    private final String msg;

    ReturnCodeEnum(String value, String msg){
        this.value = value;
        this.msg = msg;
    }

    /**
     * @return 获取枚举值
     */
    @Override
    public String getValue() {
        return this.value;
    }

    /**
     * @return 获取枚举描述
     */
    @Override
    public String getMsg() {
        return this.msg;
    }

    /**
     * @return 获取枚举名称对应的枚举值
     */
    @Override
    public HashMap<String, String> getMaps() {
        HashMap<String, String> map =  new HashMap<>(ReturnCodeEnum.values().length);
        for (ReturnCodeEnum c: ReturnCodeEnum.values()) {
            map.put(c.name(), c.getValue());
        }
        return map;
    }
}
