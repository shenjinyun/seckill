package com.xxx.seckill.exception;

import com.xxx.seckill.vo.ResponseBean;
import com.xxx.seckill.vo.ResponseBeanEnum;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;



/**
 * 全局异常处理类
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseBean ExceptionHandler(Exception e) {
        if(e instanceof GlobalException) {
            GlobalException globalException = (GlobalException) e;
            return ResponseBean.error(globalException.getResponseBeanEnum());
        } else if(e instanceof BindException) {
            BindException bindException = (BindException) e;
            ResponseBean responseBean = ResponseBean.error(ResponseBeanEnum.BIND_ERROR);
            responseBean.setMessage("参数校验异常: " + bindException.getBindingResult().getAllErrors().get(0).getDefaultMessage());
            return responseBean;
        }
        return ResponseBean.error(ResponseBeanEnum.ERROR);
    }
}
