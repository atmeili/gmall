package com.atguigu.gmall.product.service;

/**
 * @author mqx
 * @date 2021-2-5 10:26:04
 */
public interface TestService {

    //  测试本地锁
    void testLock();

    // 写锁
    String writeLock();
    //  读锁
    String readLock();

}
