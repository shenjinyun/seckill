package com.xxx.seckill.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xxx.seckill.pojo.Order;
import com.xxx.seckill.pojo.SeckillMessage;
import com.xxx.seckill.pojo.SeckillOrder;
import com.xxx.seckill.pojo.User;
import com.xxx.seckill.rabbitmq.MQSender;
import com.xxx.seckill.service.IGoodsService;
import com.xxx.seckill.service.IOrderService;
import com.xxx.seckill.service.ISeckillOrderService;
import com.xxx.seckill.utils.JsonUtil;
import com.xxx.seckill.vo.GoodsVo;
import com.xxx.seckill.vo.ResponseBean;
import com.xxx.seckill.vo.ResponseBeanEnum;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/seckill")
public class SecKillController implements InitializingBean {

    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private ISeckillOrderService seckillOrderService;
    @Autowired
    private IOrderService orderService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MQSender mqSender;

    private Map<Long, Boolean> EmptyStockMap = new HashMap<>();
    /*
    * 功能描述：秒杀
    * linux优化前QPS 97.8
    *
    */
    @RequestMapping("/doSeckill2")
    public String doSecKill2(Model model, User user, Long goodsId) {
        if(user == null) {
            return "login";
        }

        model.addAttribute("user", user);
        GoodsVo goods = goodsService.findGoodsVoByGoodsId(goodsId);

        // 判断库存
        if(goods.getStockCount() < 1) {
            model.addAttribute("errmsg", ResponseBeanEnum.EMPTY_STOCK.getMessage());
            return "secKillFail";
        }

        // 判断是否重复抢购
        SeckillOrder seckillOrder = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>().eq("user_id", user.getId()).eq(("goods_id"), goodsId));
        if(seckillOrder != null) {
            model.addAttribute("errmsg", ResponseBeanEnum.REPEAT_ERROR.getMessage());
            return "SecKillFail";
        }

        Order order = orderService.seckill(user, goods);
        model.addAttribute("order", order);
        model.addAttribute("goods", goods);
        return "orderDetail";
    }

    /*
     * 功能描述：秒杀
     * linux优化前QPS 97.8
     * linux优化后QPS 210.9
     *
     */
    @RequestMapping(value = "/doSeckill", method = RequestMethod.POST)
    @ResponseBody
    public ResponseBean doSecKill(Model model, User user, Long goodsId) {
        if(user == null) {
            return ResponseBean.error(ResponseBeanEnum.SESSION_ERROR);
        }

        ValueOperations valueOperations = redisTemplate.opsForValue();
        // 判断是否重复抢购

        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);
        if(seckillOrder != null) {
            // model.addAttribute("errmsg", ResponseBeanEnum.REPEAT_ERROR.getMessage());
            return ResponseBean.error(ResponseBeanEnum.REPEAT_ERROR);
        }

        // 内存标记，减少redis访问
        if(EmptyStockMap.get(goodsId)) {
            return ResponseBean.error(ResponseBeanEnum.EMPTY_STOCK);
        }

        // 预减库存，原子操作
        Long stock = valueOperations.decrement("seckillGoods:" + goodsId);
        if(stock < 0) {
            EmptyStockMap.put(goodsId, true);
            valueOperations.increment("seckillGoods:" + goodsId);
            return ResponseBean.error(ResponseBeanEnum.EMPTY_STOCK);
        }

        SeckillMessage seckillMessage = new SeckillMessage(user, goodsId);
        mqSender.sendSeckillMessage(JsonUtil.object2JsonStr(seckillMessage));
        return ResponseBean.success(0);

        /*
        GoodsVo goods = goodsService.findGoodsVoByGoodsId(goodsId);

        // 判断库存
        if(goods.getStockCount() < 1) {
            // model.addAttribute("errmsg", ResponseBeanEnum.EMPTY_STOCK.getMessage());
            return ResponseBean.error(ResponseBeanEnum.EMPTY_STOCK);
        }

        // 判断是否重复抢购
        // SeckillOrder seckillOrder = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>().eq("user_id", user.getId()).eq(("goods_id"), goodsId));

        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);
        if(seckillOrder != null) {
            // model.addAttribute("errmsg", ResponseBeanEnum.REPEAT_ERROR.getMessage());
            return ResponseBean.error(ResponseBeanEnum.REPEAT_ERROR);
        }

        Order order = orderService.seckill(user, goods);

        return ResponseBean.success(order);
         */
    }

    /**
     * 获取秒杀结果
     * @param user
     * @param goodsId
     * @return orderId:成功 -1：秒杀失败 0：排队中
     */
    @RequestMapping(value = "/result", method = RequestMethod.GET)
    @ResponseBody
    public ResponseBean getResult(User user, Long goodsId) {
        if(user == null) {
            return ResponseBean.error(ResponseBeanEnum.SESSION_ERROR);
        }
        Long orderId = seckillOrderService.getResult(user, goodsId);
        return ResponseBean.success(orderId);
    }

    /**
     * 系统初始化：把商品库存数量加载到redis
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> list = goodsService.findGoodsVo();
        if(CollectionUtils.isEmpty(list)) {
            return;
        }

        list.forEach(goodsVo -> {
                    redisTemplate.opsForValue().set("seckillGoods:" + goodsVo.getId(), goodsVo.getStockCount());
                    EmptyStockMap.put(goodsVo.getId(), false);
            }
        );
    }
}
