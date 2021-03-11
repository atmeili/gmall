package com.atguigu.gmall.user.client;

import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.user.client.impl.UserDegradeFeignClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * @author mqx
 * @date 2021-2-27 15:11:13
 */
@FeignClient(value = "service-user",fallback = UserDegradeFeignClient.class)
public interface UserFeignClient {

    //  根据用户Id 查询收货地址列表
    @GetMapping("/api/user/inner/findUserAddressListByUserId/{userId}")
    List<UserAddress> findUserAddressListByUserId(@PathVariable String userId);

}
