package com.atguigu.gmall.payment.client.impl;

import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.payment.client.PaymentFeignClient;
import org.springframework.stereotype.Component;

/**
 * @author mqx
 * @date 2021-3-6 14:11:18
 */
@Component
public class PaymentFeignClientImpl implements PaymentFeignClient {
    @Override
    public PaymentInfo getPaymentInfo(String outTradeNo) {
        return null;
    }

    @Override
    public Boolean checkPayment(Long orderId) {
        return null;
    }

    @Override
    public Boolean closePay(Long orderId) {
        return null;
    }
}
