package com.xxx.seckill.controller;

import com.xxx.seckill.pojo.User;
import com.xxx.seckill.service.IGoodsService;
import com.xxx.seckill.service.IUserService;
import com.xxx.seckill.vo.DetailVo;
import com.xxx.seckill.vo.GoodsVo;
import com.xxx.seckill.vo.ResponseBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;
import org.thymeleaf.util.StringUtils;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    private IUserService userService;

    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ThymeleafViewResolver thymeleafViewResolver;

    /**
     * 跳转到商品列表页
     * Linux 优化前QPS 174.4
     *       页面缓存QPS 203.4
     *
     * @param
     * @param model
     * @param
     * @return
     */

    // 页面缓存
    @RequestMapping(value = "/toList", produces = "text/html; charset=utf-8")
    @ResponseBody
    public String toList(Model model, User user,
                         HttpServletRequest request, HttpServletResponse response) {
        // Redis中获取页面，如果不为空，直接返回页面
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String html = (String) valueOperations.get("goodsList");
        if(!StringUtils.isEmpty(html)) {
            return html;
        }
//        if(StringUtils.isEmpty(ticket)) {
//            return "login";
//        }
//        // User user = (User) session.getAttribute(ticket);
//        User user = userService.getUserByCookie(ticket, request, response);
//        if(null == user) {
//            return "login";
//        }
//
        model.addAttribute("user", user);
        model.addAttribute("goodsList", goodsService.findGoodsVo());

        // return "goodsList";
        // 如果为空，手动渲染，存入Redis并返回
        WebContext webContext = new WebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goodsList", webContext);
        if(!StringUtils.isEmpty(html)) {
            valueOperations.set("goodsList", html, 60, TimeUnit.SECONDS);
        }
        return html;
    }

    /**
     * 跳转商品详情页
     * @param goodsId
     * @return
     */

    // URL缓存，本质也是页面缓存
    @RequestMapping(value = "/toDetail2/{goodsId}", produces = "text/html;charset=utf-8")
    @ResponseBody
    public String toDetail2(Model model, User user, @PathVariable Long goodsId,
                           HttpServletRequest request, HttpServletResponse  response) {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        // Redis中获取页面，如果不为空，返回页面
        String html = (String) valueOperations.get("goodsDetail:" + goodsId);
        if(!StringUtils.isEmpty(html)) {
            return  html;
        }

        model.addAttribute("user", user);

        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
        Date startDate = goodsVo.getStartDate();
        Date endDate = goodsVo.getEndDate();
        Date nowDate = new Date();
        //秒杀状态
        int secKillStatus = 0;
        // 秒杀倒计时
        int remainSeconds = 0;
        if(nowDate.before(startDate)) {
            // 秒杀还未开始
            secKillStatus = 0;
            remainSeconds = (int) ((startDate.getTime() - nowDate.getTime()) / 1000);

        }else if(nowDate.after(endDate)) {
            // 秒杀已结束
            secKillStatus = 2;
            remainSeconds = -1;
        } else {
            // 秒杀中
            secKillStatus = 1;
            remainSeconds = 0;

        }
        model.addAttribute("remainSeconds", remainSeconds);
        model.addAttribute("secKillStatus", secKillStatus);
        model.addAttribute("goods", goodsVo);

        WebContext webContext = new WebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goodsDetail", webContext);
        if(!StringUtils.isEmpty(html)) {
            valueOperations.set("goodsDetail:" + goodsId, html, 60, TimeUnit.SECONDS);

        }
        return html;
    }

    /**
     * 跳转商品详情页
     * @param goodsId
     * @return
     */

    // URL缓存，本质也是页面缓存
    @RequestMapping("/detail/{goodsId}")
    @ResponseBody
    public ResponseBean toDetail(Model model, User user, @PathVariable Long goodsId) {
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
        Date startDate = goodsVo.getStartDate();
        Date endDate = goodsVo.getEndDate();
        Date nowDate = new Date();
        int secKillStatus = 0;
        //秒杀倒计时
        int remainSeconds = 0;
        if(nowDate.before(startDate)) {
            secKillStatus = 0;
            remainSeconds = (int)((startDate.getTime() - nowDate.getTime()) / 1000);
        } else if(nowDate.after(endDate)) {
            secKillStatus = 2;
            remainSeconds = -1;
        } else {
            secKillStatus = 1;
            remainSeconds = 0;
        }
        DetailVo detailVo = new DetailVo();
        detailVo.setUser(user);
        detailVo.setGoodsVo(goodsVo);
        detailVo.setRemainSeconds(remainSeconds);
        detailVo.setSecKillStatus(secKillStatus);
        return ResponseBean.success(detailVo);
    }
}
