package com.xxx.seckill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xxx.seckill.exception.GlobalException;
import com.xxx.seckill.mapper.UserMapper;
import com.xxx.seckill.pojo.User;
import com.xxx.seckill.service.IUserService;
import com.xxx.seckill.utils.CookieUtil;
import com.xxx.seckill.utils.MD5Util;
import com.xxx.seckill.utils.UUIDUtil;
import com.xxx.seckill.vo.LoginVo;
import com.xxx.seckill.vo.ResponseBean;
import com.xxx.seckill.vo.ResponseBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author joey
 * @since 2022-04-25
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 登录
     * @param loginVo
     * @param request
     * @param response
     * @return
     */
    @Override
    public ResponseBean doLogin(LoginVo loginVo, HttpServletRequest request, HttpServletResponse response) {
        String mobile = loginVo.getMobile();
        String password = loginVo.getPassword();
        // 参数校验
//        if(StringUtils.isEmpty(mobile) || StringUtils.isEmpty(password)) {
//            return ResponseBean.eroor(ResponseBeanEnum.LOGIN_ERROR);
//        }
//
//        if(!ValidatorUtil.isMobile(mobile)) {
//            return ResponseBean.eroor(ResponseBeanEnum.MOBILE_ERROR);
//        }

        // 根据手机号获取用户
        User user = userMapper.selectById(mobile);
        if(null == user) {
            throw new GlobalException(ResponseBeanEnum.LOGIN_ERROR);
            // return ResponseBean.eroor(ResponseBeanEnum.LOGIN_ERROR);
        }

        // 判断密码是否正确
        if(!MD5Util.fromPassToDBPass(password, user.getSalt()).equals(user.getPassword())) {
            throw new GlobalException(ResponseBeanEnum.LOGIN_ERROR);
            //return ResponseBean.eroor(ResponseBeanEnum.LOGIN_ERROR);
        }
        // 生成cookie
        String ticket = UUIDUtil.uuid();
        // 将用户信息存入redis
        redisTemplate.opsForValue().set("user:" + ticket, user);
        // request.getSession().setAttribute(ticket, user);
        CookieUtil.setCookie(request, response, "userTicket", ticket);
        return ResponseBean.success(ticket);
    }

    /**
     * 根据cookie获取用户
     * @param userTicket
     * @return
     */
    @Override
    public User getUserByCookie(String userTicket, HttpServletRequest request, HttpServletResponse response) {
        if(StringUtils.isEmpty(userTicket)) {
            return null;
        }
        User user = (User) redisTemplate.opsForValue().get("user:" + userTicket);
        if(user != null) {
            CookieUtil.setCookie(request, response, "userTicket", userTicket);
        }
        return user;
    }

    /**
     * 更新密码
     * @param userTicket
     * @param password
     * @param request
     * @param response
     * @return
     */
    @Override
    public ResponseBean updatePassword(String userTicket, String password,
                                       HttpServletRequest request, HttpServletResponse response) {
        User user = getUserByCookie(userTicket, request, response);
        if(user == null) {
            throw new GlobalException(ResponseBeanEnum.MOBILE_NOT_EXIST);
        }
        user.setPassword(MD5Util.inputPassToDBPass(password, user.getSalt()));
        int result = userMapper.updateById(user);
        if(1 == result) {
            // 删除Redis
            redisTemplate.delete("user:" + userTicket);
            return ResponseBean.success();
        }
        return ResponseBean.error(ResponseBeanEnum.PASSWORD_UPDATE_FAIL);
    }
}
