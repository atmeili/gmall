package com.atguigu.gmall.user.service;

import com.atguigu.gmall.model.user.UserAddress;

import java.util.List;

/**
 * @author mqx
 * @date 2021-2-27 14:48:29
 */
public interface UserAddressService {

    //  根据用户Id 查询用户收货地址列表
    List<UserAddress> findUserAddressListById(String userId);
}
