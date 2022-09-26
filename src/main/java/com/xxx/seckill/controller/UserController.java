package com.xxx.seckill.controller;


import com.xxx.seckill.pojo.User;
import com.xxx.seckill.rabbitmq.MQSender;
import com.xxx.seckill.vo.ResponseBean;
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
 * @since 2022-04-25
 */
@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private MQSender mqSender;

    /**
     * 用户信息（测试）
     * @param user
     * @return
     */
    @RequestMapping("/info")
    @ResponseBody
    public ResponseBean info(User user) {
        return ResponseBean.success(user);
    }

    /**
     * 测试发送RabbitMQ消息
     */
    @RequestMapping("/mq")
    @ResponseBody
    public void mq() {
        mqSender.send("hello");
    }

    /**
     * Fanout模式
     */
    @RequestMapping("/mq/fanout")
    @ResponseBody
    public void mq01() {
        mqSender.send("hello");
    }

    /**
     * Direct模式
     */
    @RequestMapping("/mq/direct01")
    @ResponseBody
    public void mq02() {
        mqSender.send01("hello, red");
    }

    /**
     * Direct模式
     */
    @RequestMapping("/mq/direct02")
    @ResponseBody
    public void mq03() {
        mqSender.send02("hello, green");
    }

    /**
     * Topic模式
     */
    @RequestMapping("/mq/topic01")
    @ResponseBody
    public void mq04() {
        mqSender.send03("hello, red");
    }

    /**
     * Topic模式
     */
    @RequestMapping("/mq/topic02")
    @ResponseBody
    public void mq05() {
        mqSender.send04("hello, green");
    }
}
