package com.xxx.seckill.exception;

import com.xxx.seckill.vo.ResponseBeanEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class GlobalException extends RuntimeException{
    private ResponseBeanEnum responseBeanEnum;
}
