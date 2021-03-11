package com.atguigu.gmall.order.controller;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.client.CartFeignClient;
import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.service.RabbitService;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.atguigu.gmall.user.client.UserFeignClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author mqx
 * @date 2021-3-1 09:07:46
 */
@RestController
@RequestMapping("api/order")
public class OrderApiController {

    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private CartFeignClient cartFeignClient;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    private RabbitService rabbitService;



    //  最终应该看页面需要什么样的数据； 需要一个userAddressList detailArrayList totalNum totalAmount
    //  api/order/auth/trade 用户访问这个带有auth 的控制器， 则必须登录！
    @GetMapping("auth/trade")
    public Result<Map<String,Object>> trade(HttpServletRequest request){
        //  获取用户Id
        String userId = AuthContextHolder.getUserId(request);
        Map<String,Object> map = new HashMap<>();
        //  获取用户收货地址列表
        List<UserAddress> userAddressList = userFeignClient.findUserAddressListByUserId(userId);

        //  获取购物车中的送货清单
        List<CartInfo> cartCheckedList = cartFeignClient.getCartCheckedList(userId);

        List<OrderDetail> orderDetails = new ArrayList<>();
        int totalNum = 0;
        //  需要将cartCheckedList 集合中的数据 赋值给orderDetail 数据
        for (CartInfo cartInfo : cartCheckedList) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setSkuId(cartInfo.getSkuId());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            orderDetail.setOrderPrice(cartInfo.getSkuPrice());
            orderDetail.setSkuNum(cartInfo.getSkuNum());
            totalNum+=cartInfo.getSkuNum();
            //  后续字段暂时不用，到优惠券部分再去添加
            orderDetails.add(orderDetail);
        }


        //  计算一下总金额：OrderInfo 实体类中有个计算总金额的！
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderDetailList(orderDetails);
        //  调用方法即可
        orderInfo.sumTotalAmount(); // 将结果赋值给 totalAmount

        map.put("userAddressList",userAddressList);
        map.put("detailArrayList",orderDetails);
        //  计算件数：
        map.put("totalNum",orderDetails.size());    //  以spu下的商品个数
        //  map.put("totalNum",totalNum);   //  以商品skuNum件数统计的
        map.put("totalAmount",orderInfo.getTotalAmount());
        //  保存流水号
        String tradeNo = orderService.getTradeNo(userId);
        //  request.setAttribute("tradeNo",tradeNo);
        map.put("tradeNo",tradeNo);
        return Result.ok(map);
    }

    //  保存订单的控制器 获取到前端传递的数据：Json ---> JavaObject; 使用@RequestBody
    //  http://api.gmall.com/api/order/auth/submitOrder?tradeNo=null
    @PostMapping("auth/submitOrder")
    public Result submitOrder(@RequestBody OrderInfo orderInfo,HttpServletRequest request){
        //  user_id 在控制器中获取即可！*****
        String userId = AuthContextHolder.getUserId(request);
        orderInfo.setUserId(Long.parseLong(userId));
        //  获取页面传递过来的流水号
        String tradeNo = request.getParameter("tradeNo");
        //  调用比较方法
        boolean flag = orderService.checkTradeNo(tradeNo, userId);
        //  判断
        if(!flag){
            //  比较失败
            return Result.fail().message("不能重复提交订单!");
        }

        //  用户提示集中到一起，放入一个字符串集合中
        List<String> errorList = new ArrayList<>();
        //  创建一个集合来存储异步编排对象
        List<CompletableFuture> futureList = new ArrayList<>();
        //  验证库存：
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        if (!CollectionUtils.isEmpty(orderDetailList)){
            //  循环遍历
            for (OrderDetail orderDetail : orderDetailList) {

                //  在这个位置获取一个异步编排对象,用户提示集中到一起，放入一个字符串集合中
                CompletableFuture<Void> checkStockCompletableFuture = CompletableFuture.runAsync(() -> {
                    //  调用验证库存的方法
                    boolean result = orderService.checkStock(orderDetail.getSkuId(), orderDetail.getSkuNum());
                    //  result = true 说明有库存， result = false 说明没有库存
                    if (!result) {
                        //  信息提示
                        //  return Result.fail().message(orderDetail.getSkuName()+"库存剩余不足!");
                        errorList.add(orderDetail.getSkuName() + "库存剩余不足!");
                    }
                }, threadPoolExecutor);

                //  将这个异步编排对象放入集合
                futureList.add(checkStockCompletableFuture);

                //  声明一个异步编排
                CompletableFuture<Void> skuPriceCompletableFuture = CompletableFuture.runAsync(() -> {
                    //  验证价格： 当前订单价格与实际商品的价格是否一直  skuInfo.price();
                    BigDecimal skuPrice = productFeignClient.getSkuPrice(orderDetail.getSkuId());
                    //  比较价格
                    if (orderDetail.getOrderPrice().compareTo(skuPrice) != 0) {
                        //  更新一下商品的价格
                        cartFeignClient.loadCartCache(userId);
                        //  提示
                        //  return  Result.fail().message(orderDetail.getSkuName()+"\t 价格有变动!");
                        errorList.add(orderDetail.getSkuName() + "\t 价格有变动!");
                    }
                }, threadPoolExecutor);
                //  将这个异步编排对象放入集合
                futureList.add(skuPriceCompletableFuture);
            }
        }
        //  使用allOf 将任务组合在一起
        //  CompletableFuture.allOf(futureList.toArray(new CompletableFuture[futureList.size()])).join();
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[futureList.size()])).join();

        //  利用errorList 这个集合来判断是否有不合法的数据
        if (errorList.size()>0){
            //  将errorList 中的数据全部拿出来显示 xxx 价格有变动！，xxx 库存不足！
            return Result.fail().message(StringUtils.join(errorList,","));
        }
        //  删除缓存的流水号
        orderService.delTradeNo(userId);

        Long orderId = orderService.saveOrderInfo(orderInfo);

        //  发送延迟消息到队列
        //  发送消息的内容与 监听消息时 处理的业务有关系！ 2秒种内支付。{根据订单Id取消订单}
        rabbitService.sendDelayMessage(MqConst.EXCHANGE_DIRECT_ORDER_CANCEL,MqConst.ROUTING_ORDER_CANCEL,orderId,MqConst.DELAY_TIME);
        //  返回订单Id
        return Result.ok(orderId);
    }

    //  根据订单Id 查询订单数据+订单明细数据
    @GetMapping("inner/getOrderInfo/{orderId}")
    public OrderInfo getOrderInfo(@PathVariable Long orderId){
        return orderService.getOrderInfo(orderId);
    }

    //  拆单控制器：
    //  http://localhost:8204/api/order/orderSplit?orderId=xxx&wareSkuMap=xxxx
    @RequestMapping("orderSplit")
    public String orderSplit(HttpServletRequest request){
        String orderId = request.getParameter("orderId");
        //  商品与仓库的关系
        //  [{"wareId":"1","skuIds":["2","10"]},{"wareId":"2","skuIds":["3"]}]
        String wareSkuMap = request.getParameter("wareSkuMap");
        //  返回子订单的json 集合字符串！ 子订单通过拆单得到！
        List<OrderInfo> subOrderInfoList = orderService.orderSplit(orderId,wareSkuMap);

        //  声明一个集合来存储map
        List<Map> maps = new ArrayList<>();

        //  判断子订单集合不为空
        if(!CollectionUtils.isEmpty(subOrderInfoList)){
            //  循环遍历
            for (OrderInfo orderInfo : subOrderInfoList) {
                //  orderInfo 变为Map
                Map map = orderService.initWareOrder(orderInfo);
                maps.add(map);
            }
        }
        // maps 中的数据是子订单集合想要的数据！
        return JSON.toJSONString(maps);
    }

    //  提交订单
    @PostMapping("inner/seckill/submitOrder")
    public Long submitOrder(@RequestBody OrderInfo orderInfo){
        //  返回订单Id
        Long orderId = orderService.saveOrderInfo(orderInfo);
        return orderId;
    }
}
