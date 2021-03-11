package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author mqx
 * @date 2021-2-5 10:24:40
 */
@RestController
@RequestMapping("admin/product/test")
public class TestController {

    @Autowired
    private TestService testService;

    @GetMapping("testLock")
    public Result testLock(){
        //  远程调用一个方法
        testService.testLock();
        return Result.ok();
    }


    // 写锁
    @GetMapping("write")
    public Result writeLock(){
        // 调用服务层方法
        String msg = testService.writeLock();
        //  返回
        return  Result.ok(msg);
    }
    // 读锁
    @GetMapping("read")
    public Result readLock(){
        // 调用服务层方法
        String msg = testService.readLock();
        //  返回
        return  Result.ok(msg);
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //  创建一个异步编排对象
        //  Supplier
        //        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(new Supplier<Integer>() {
        //            @Override
        //            public Integer get() {
        //                System.out.println("来人了，开始接客了.");
        //                // int i = 1/0;
        //                //  返回值
        //                return 1024;
        //            }
        //        }).thenApply(new Function<Integer, Integer>() {
        //            @Override
        //            public Integer apply(Integer integer) {
        //                //  获取到上一个结果集，并返回自己运算的结果
        //                System.out.println(integer+"\t 哈哈哈");
        //
        //                return integer*2;
        //            }
        //        }).whenComplete(new BiConsumer<Integer, Throwable>() {
        //            @Override
        //            public void accept(Integer o, Throwable throwable) {
        //                System.out.println(o + "ooooooo");
        //                System.out.println(throwable + "throwable");
        //
        //            }
        //        }).exceptionally(new Function<Throwable, Integer>() {
        //            @Override
        //            public Integer apply(Throwable throwable) {
        //                System.out.println(throwable+"==========throwable");
        //                return 404;
        //            }
        //        });
        //
        //        System.out.println(future.get());

        //  自定义线程池
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                2,
                5,
                3,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(3)
        );
        //  Supplier T get(); 线程futureA
        CompletableFuture<String> futureA = CompletableFuture.supplyAsync(() -> "hello");

        // 线程futureB 利用futureA的返回值结果 ,自己不再做任何运算
        // Consumer void accept(T t);
        CompletableFuture<Void> futureB = futureA.thenAcceptAsync((s) -> {
            //   设置一个延迟
            delaySec(3);
            //   直接打印结果
            printStr(s + "\t futureB");
        },threadPoolExecutor);

        // 线程futureC
        CompletableFuture<Void> futureC = futureA.thenAcceptAsync((s) -> {
            //   设置一个延迟
            delaySec(1);
            //   直接打印结果
            printStr(s + "\t futureC");
        },threadPoolExecutor);

        //  如果B,C 并行的话，那么谁睡眠时间短，就应该先执行谁！
        System.out.println(futureB.get());
        System.out.println(futureC.get());


    }
    //  打印方法
    private static void printStr(String str) {
        System.out.println(str);

    }

    // 睡眠方法
    private static void delaySec(int i) {
        try {
            Thread.sleep(i*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
