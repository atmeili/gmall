package com.atguigu.gmall.payment.service;

import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.payment.PaymentInfo;

import java.util.Map;

/**
 * @author mqx
 * @date 2021-3-3 14:25:45
 */
public interface PaymentService {

    //  保存交易记录信息 需要根据订单Id ，查询到paymentInfo需要的数据
    //  out_trade_no，order_id，payment_type,total_amount,subject,payment_status,create_time
    //  通过orderId 获取orderInfo 中对应的数据即可！

    /**
     * 保存支付交易接口
     * @param orderInfo
     * @param paymentType
     */
    void savePaymentInfo(OrderInfo orderInfo , String paymentType);

    /**
     * 根据outTradeNo ,支付方式查询交易记录
     * @param outTradeNo
     * @param name
     * @return
     */
    PaymentInfo getPaymentInfo(String outTradeNo, String name);

    /**
     * 付款成功：调用更新交易状态
     * @param outTradeNo
     * @param name
     * @param paramMap
     */
    void paySuccess(String outTradeNo, String name, Map<String, String> paramMap);

    /**
     * 更新交易记录状态
     * @param outTradeNo
     * @param name
     * @param paymentInfo
     */
    void updatePaymentInfo(String outTradeNo, String name, PaymentInfo paymentInfo);

    /**
     * 根据订单Id 关闭交易记录
     * @param orderId
     */
    void closePayment(Long orderId);
}
