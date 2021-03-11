package com.atguigu.gmall.task.schedule;

import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.common.service.RabbitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author mqx
 * @date 2021-3-6 15:55:07
 */
//  将当前的类注入到spring 容器中,开启定时任务
@Component
@EnableScheduling
public class ScheduledTask {

    @Autowired
    private RabbitService rabbitService;
    //  编写定时任务
    //  每隔10秒触发一次任务
    @Scheduled(cron = "0/10 * * * * ?")
    public void task(){
        //  System.out.println("来人了，开始接客吧!");
        //  发送一个消息： 任意一个字符串都可以！
        //  在消费的时候，跟这个发送订单内容没有关系！ 消费的时候，只查询当天的秒杀商品即可
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_TASK,MqConst.ROUTING_TASK_1,"来吧!");
    }
    @Scheduled(cron = "0 0 18 * * ?")
    public void clearData(){
        //  System.out.println("来人了，开始接客吧!");
        //  发送一个消息： 任意一个字符串都可以！
        //  在消费的时候，只需要清空缓存数据！
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_TASK,MqConst.ROUTING_TASK_18,"走吧!");
    }
}
