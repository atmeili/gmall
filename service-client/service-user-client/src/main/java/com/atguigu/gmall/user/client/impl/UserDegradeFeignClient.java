package com.atguigu.gmall.user.client.impl;

import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.user.client.UserFeignClient;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author mqx
 * @date 2021-2-27 15:11:39
 */
@Component
public class UserDegradeFeignClient implements UserFeignClient {
    @Override
    public List<UserAddress> findUserAddressListByUserId(String userId) {
        return null;
    }
}
