package com.atguigu.gmall.all.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * @author mqx
 * @date 2021-2-24 11:34:05
 */
@Controller
public class PassportController {

    //  用户在访问什么的时候会跳转到登录页面！
    //  http://passport.gmall.com/login.html?originUrl=http://www.gmall.com/
    @GetMapping("login.html")
    public String login(HttpServletRequest request){
        String originUrl = request.getParameter("originUrl");
        request.setAttribute("originUrl",originUrl);
        return "login";
    }

}
