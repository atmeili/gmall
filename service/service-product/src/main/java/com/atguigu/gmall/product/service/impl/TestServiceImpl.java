package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.product.service.TestService;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author mqx
 * @date 2021-2-5 10:26:29
 */
@Service
public class TestServiceImpl implements TestService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public void testLock() {
        //  获取对象
        RLock lock = redissonClient.getLock("lock");
        //  加锁
        //  lock.lock();
        //  10 后自动解锁
        lock.lock(10,TimeUnit.SECONDS);

        //  业务逻辑
        String value = redisTemplate.opsForValue().get("num");

        //  判断value是否为空
        if (StringUtils.isEmpty(value)){
            return;
        }
        //  数据转换
        int num = Integer.parseInt(value);
        //  放入缓存
        redisTemplate.opsForValue().set("num",String.valueOf(++num));
        //  解锁
        //  lock.unlock();

    }

    @Override
    public String writeLock() {
        //  定义读写锁对象
        RReadWriteLock rwlock = redissonClient.getReadWriteLock("anyRWLock");
        //  获取写锁的对象
        RLock rLock = rwlock.writeLock();
        //  表示10秒自动解锁
        rLock.lock(10,TimeUnit.SECONDS);
        // 定义一个写入的数据
        String uuid = UUID.randomUUID().toString();
        //  写入缓存
        redisTemplate.opsForValue().set("msg",uuid);

        return "写入数据完成.....";
    }

    @Override
    public String readLock() {
        //  定义读写锁对象
        RReadWriteLock rwlock = redissonClient.getReadWriteLock("anyRWLock");
        //  获取读锁对象
        RLock rLock = rwlock.readLock();
        // 设置锁的时间
        rLock.lock(10,TimeUnit.SECONDS);

        // 读取缓存中对应的msg
        String msg = redisTemplate.opsForValue().get("msg");

        return msg;
    }


    //  synchronized 不要了！
    //    @Override
    //    public void testLock() {
    //        //  setnx lock OK  原来使用的是固定值 OK
    //        //  Boolean flag = redisTemplate.opsForValue().setIfAbsent("lock", "OK");
    //        //  set lock OK px 1000 nx  原来使用的是固定值 OK
    //        //  Boolean flag = redisTemplate.opsForValue().setIfAbsent("lock", "OK",3, TimeUnit.SECONDS);
    //        //  使用UUID 防止误删锁
    //        //  原来使用的是固定值 OK
    //        //  set lock uuid px 1000 nx
    //        String uuid = UUID.randomUUID().toString();
    //        //  缓存的lock 对应的值 ，应该是index2 的uuid
    //
    //        Boolean flag = redisTemplate.opsForValue().setIfAbsent("lock", uuid,1, TimeUnit.SECONDS);
    //        //  判断flag index=1
    //        if (flag){
    //            //  说明上锁成功！ 执行业务逻辑
    //            /*
    //            1.  先获取缓存中的数据 这个key=num  set num 0
    //            2.  如果获取到了数据，则对这个数据进行+1操作,再次放入缓存
    //            3.  如果没有获取到数据，则直接返回
    //                    */
    //            String value = redisTemplate.opsForValue().get("num");
    //            //  判断
    //            if(StringUtils.isEmpty(value)){
    //                return;
    //            }
    //            // 故意异常！
    //            // int i = 1/0;
    //            //  数据类型转换
    //            int num = Integer.parseInt(value);
    //            //  放入缓存
    //            redisTemplate.opsForValue().set("num",String.valueOf(++num));
    //
    //            //  判断 缓存中的uuid 与 代码块生产的uuid 是否一致!
    //            //            if (redisTemplate.opsForValue().get("lock").equals(uuid)){
    //            //                //  index1 执行完成之后，删除操作 ： 先判断lock的值 与uuid 是否相当！
    //            //                //  比较成功才会执行删除key！  index 已经比较完成了，准备执行del key的时候，此时CPU 将执行权限让出去了。
    //            //                //  同时index1 的key到了过期时间，index2 进来了。cpu 直接将执行权限给index2
    //            //                //  cpu 又将执行权限还给了index1
    //            //                //  删除锁
    //            //                redisTemplate.delete("lock");
    //            //            }
    //
    //            //  定义一个lua 脚本
    //            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
    //
    //            //  准备执行lua 脚本
    //            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
    //            //  将lua脚本放入DefaultRedisScript 对象中
    //            redisScript.setScriptText(script);
    //            //  设置DefaultRedisScript 这个对象的泛型
    //            redisScript.setResultType(Long.class);
    //            //  执行删除
    //            redisTemplate.execute(redisScript, Arrays.asList("lock"),uuid);
    //
    //        }else {
    //            //  没有获取到锁！
    //            try {
    //                Thread.sleep(1000);
    //                //  睡醒了之后，重试
    //                testLock();
    //            } catch (InterruptedException e) {
    //                e.printStackTrace();
    //            }
    //        }
    //    }
}
