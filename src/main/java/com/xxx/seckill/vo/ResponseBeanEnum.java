package com.xxx.seckill.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;


/**
 * 公共返回对象枚举
 */
@Getter
@ToString
@AllArgsConstructor
public enum ResponseBeanEnum {
    // 通用
    SUCCESS(200, "success"),
    ERROR(500, "服务端异常"),

    // 登录模块5002XX
    LOGIN_ERROR(500210, "用户名或密码错误"),
    MOBILE_ERROR(500211, "手机号码格式错误"),
    BIND_ERROR(500212, "参数校验异常"),
    MOBILE_NOT_EXIST(500213, "手机号码不存在"),
    PASSWORD_UPDATE_FAIL(500214, "密码更新失败"),
    SESSION_ERROR(500215, "用户不存在"),

    // 秒杀模块5005XX
    EMPTY_STOCK(500500, "库存不足"),
    REPEAT_ERROR(500501, "该商品限购一件"),

    // 订单模块5003XX
    ORDER_NOT_EXIST(500300, "订单信息不存在")
    ;


    private final Integer code;
    private final String message;
}
