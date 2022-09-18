package com.xxx.seckill.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 公共返回对象
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseBean {


    private long code;
    private String message;
    private Object obj;

    /**
     * 成功返回结果，一般是200
     * @return
     */
    public static ResponseBean success() {
        return new ResponseBean(ResponseBeanEnum.SUCCESS.getCode(), ResponseBeanEnum.SUCCESS.getMessage(), null);
    }

    public static ResponseBean success(Object obj) {
        return new ResponseBean(ResponseBeanEnum.SUCCESS.getCode(), ResponseBeanEnum.SUCCESS.getMessage(), obj);
    }

    /**
     * 失败返回结果，各有不同，400，403，500，。。。。
     * @param responseBeanEnum
     * @return
     */
    public static ResponseBean error(ResponseBeanEnum responseBeanEnum) {
        return new ResponseBean(responseBeanEnum.getCode(), responseBeanEnum.getMessage(), null);
    }

    public static ResponseBean error(ResponseBeanEnum responseBeanEnum ,Object obj) {
        return new ResponseBean(responseBeanEnum.getCode(), responseBeanEnum.getMessage(), obj);
    }


}
