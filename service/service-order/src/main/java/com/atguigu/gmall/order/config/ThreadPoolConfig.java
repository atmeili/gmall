package com.atguigu.gmall.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author mqx
 * @date 2021-2-22 11:54:18
 */
@Configuration
public class ThreadPoolConfig {

    /*
    <bean  id ="threadPoolExecutor" class="java.util.concurrent.ThreadPoolExecutor">
    </bean>
     */
    @Bean
    public ThreadPoolExecutor threadPoolExecutor(){
        //  自定义线程池
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                2,
                5,
                3,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(3)
        );
        return threadPoolExecutor;
    }

}
