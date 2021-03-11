package com.atguigu.gmall.activity.redis;

import com.atguigu.gmall.activity.util.CacheHelper;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class MessageReceive {

    /**接收消息的方法  stat:0 */
    public void receiveMessage(String message){
        System.out.println("----------收到消息了message："+message);
        if(!StringUtils.isEmpty(message)) {
            /*
             消息格式
                skuId:0 表示没有商品
                skuId:1 表示有商品
             */
            //  将双引号变为空 发送消息的时候，redis 发送的消息格式 ""6:1"" 表示商品skuId = 6 ,这个商品可以秒杀
            //  ""6:0"" 表示商品skuId = 6 ,这个商品不可以秒杀
            message = message.replaceAll("\"","");
            String[] split = StringUtils.split(message, ":");
//            String[] split = message.split(":");

            if (split == null || split.length == 2) {
                CacheHelper.put(split[0], split[1]);
            }
        }
    }

}
