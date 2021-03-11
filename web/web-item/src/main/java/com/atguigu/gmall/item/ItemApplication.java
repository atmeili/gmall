package com.atguigu.gmall.item;

/**
 * @author mqx
 * @date 2021-2-4 14:42:03
 */

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)//取消数据源自动配置
@ComponentScan({"com.atguigu.gmall"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages= {"com.atguigu.gmall"})
public class ItemApplication {

    public static void main(String[] args) {
        SpringApplication.run(ItemApplication.class, args);
    }
}
