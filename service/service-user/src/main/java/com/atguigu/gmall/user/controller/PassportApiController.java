package com.atguigu.gmall.user.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.IpUtil;
import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.service.UserService;
import org.apache.ibatis.executor.loader.ResultLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author mqx
 * @date 2021-2-24 10:26:19
 */
@RestController
@RequestMapping("/api/user/passport")
public class PassportApiController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 获取到前台传递的用户名，密码
     * @param userInfo
     * @return
     */
    @PostMapping("login")
    public Result login(@RequestBody UserInfo userInfo, HttpServletRequest request){
        /*
        1.  调用服务层登录方法
        2.  需要产生一个token
        3.  需要将用户信息保存到缓存中
         */
        UserInfo info = userService.login(userInfo);
        if (info!=null){
            //  说明用户在数据库中存在登录成功
            String token = UUID.randomUUID().toString();
            //  需要在页面显示用户昵称
            String nickName = info.getNickName();
            HashMap<String, Object> map = new HashMap<>();
            //  存储数据
            map.put("token",token);
            map.put("nickName",nickName);

            //  当用户登录成功之后，我们需要将数据放入缓存！目的是为了判断用户在访问其他业务的时候，是否登录了。
            JSONObject jsonObject = new JSONObject();
            //  存储用户Id
            jsonObject.put("userId",info.getId().toString());
            //  存储当前的Ip 地址防止用户盗用cookie 中的token！
            jsonObject.put("ip", IpUtil.getIpAddress(request));
            //  放入缓存：
            //  key = user:login:token
            String userLoginKey = RedisConst.USER_LOGIN_KEY_PREFIX+token;
            redisTemplate.opsForValue().set(userLoginKey,jsonObject.toJSONString(),RedisConst.USERKEY_TIMEOUT, TimeUnit.SECONDS);

            return Result.ok(map);
        }else {
            return Result.fail().message("登录失败!");
        }
    }

    //  退出方法
    @GetMapping("logout")
    public Result logout(HttpServletRequest request){
        //  获取到的token、在登录的时候将token放入了cookie 中，同时还将token 放入了header 中。
        String token = request.getHeader("token");
        //  登录成功的时候，将数据保存到了缓存中，直接删除缓存数据即可！
        String userLoginKey = RedisConst.USER_LOGIN_KEY_PREFIX+token;
        redisTemplate.delete(userLoginKey);
        //  返回
        return Result.ok();
    }
}
