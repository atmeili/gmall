package com.atguigu.gmall.user.service;

import com.atguigu.gmall.model.user.UserInfo;

/**
 * @author mqx
 * @date 2021-2-24 10:16:12
 */
public interface UserService {

    //  select * from user_info where username = ? and password = ?
    UserInfo login(UserInfo userInfo);
}
