package com.atguigu.gmall.order.service;

import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * @author mqx
 * @date 2021-3-1 10:46:34
 */
public interface OrderService extends IService<OrderInfo> {

    //  保存数据
    Long saveOrderInfo(OrderInfo orderInfo);

    //  生成流水号
    String getTradeNo(String userId);

    //  比较流水号
    boolean checkTradeNo(String tradeNo,String userId);

    //  删除流水号
    void delTradeNo(String userId);

    //  验证库存
    boolean checkStock(Long skuId, Integer skuNum);

    //  关闭过期订单
    void execExpiredOrder(Long orderId);

    //  根据订单Id ，进度状态更新订单数据
    void updateOrderStatus(Long orderId, ProcessStatus processStatus);

    //  根据orderId 查询订单信息
    OrderInfo getOrderInfo(Long orderId);

    //  发送消息给库存系统！
    void sendOrderStatus(Long orderId);

    //  将orderInfo 变为map
    Map initWareOrder(OrderInfo orderInfo);

    //  拆单订单
    List<OrderInfo> orderSplit(String orderId, String wareSkuMap);

    //  关闭过期订单
    void execExpiredOrder(Long orderId, String flag);
}
