package com.atguigu.gmall.user.service.impl;

import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.mapper.UserInfoMapper;
import com.atguigu.gmall.user.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

/**
 * @author mqx
 * @date 2021-2-24 10:17:49
 */
@Service
public class UserServiceImpl implements UserService {

    // 注入mapper层
    @Autowired
    private UserInfoMapper userInfoMapper;

    @Override
    public UserInfo login(UserInfo userInfo) {
        //  select * from user_info where username = ? and password = ?
        //  用户名，密码只能匹配一个对象  atguigu 96e79218965eb72c92a549dd5a330112
        //  获取到的是明文,通过MD5加密与数据库进行匹配。
        String passwd = userInfo.getPasswd();
        String newPassword = DigestUtils.md5DigestAsHex(passwd.getBytes());
        QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
        userInfoQueryWrapper.eq("login_name",userInfo.getLoginName());
        userInfoQueryWrapper.eq("passwd",newPassword);
        UserInfo info = userInfoMapper.selectOne(userInfoQueryWrapper);
        //  判断
        if (info!=null){
            return info;
        }
        return null;
    }
}
