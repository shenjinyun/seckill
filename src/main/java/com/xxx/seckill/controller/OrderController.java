package com.xxx.seckill.controller;


import com.xxx.seckill.pojo.User;
import com.xxx.seckill.service.IOrderService;
import com.xxx.seckill.vo.OrderDetailVo;
import com.xxx.seckill.vo.ResponseBean;
import com.xxx.seckill.vo.ResponseBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author joey
 * @since 2022-04-27
 */
@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private IOrderService orderService;
    /**
     * 订单详情
     * @param user
     * @param orderId
     * @return
     */

    @RequestMapping("/detail")
    @ResponseBody
    public ResponseBean detail(User user, Long orderId) {
        if(user == null) {
            return ResponseBean.error(ResponseBeanEnum.SESSION_ERROR);
        }

        OrderDetailVo detail = orderService.detail(orderId);
        return ResponseBean.success(detail);
    }
}
