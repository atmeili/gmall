package com.atguigu.gmall.common.cache;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.constant.RedisConst;
import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * @author mqx
 * @date 2021-2-19 14:41:47
 */
@Component
@Aspect
public class GmallCacheAspect {

    @Autowired
    private RedissonClient redissonClient;

    // redis 宕机了，redisTemplate也不能使用了?
    @Autowired
    private RedisTemplate redisTemplate;
    //  编写一个环绕通知
    @SneakyThrows // lombok 处理异常的注解
    @Around("@annotation(com.atguigu.gmall.common.cache.GmallCache)")
    public Object cacheAround(ProceedingJoinPoint joinPoint){
        /*
            1.  获取方法上的注解
            2.  获取到注解的前缀 组成缓存的key
            3.  根据这个key 获取缓存数据
                true:
                    则直接返回
                false:
                    则需要查询数据库，并防止缓存击穿+防止缓存穿透
         */
        Object object = new Object();
        //  将其转换为方法级别
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        // 获取到了注解
        GmallCache gmallCache = signature.getMethod().getAnnotation(GmallCache.class);
        //  获取注解前缀
        String prefix = gmallCache.prefix();
        //  组成缓存的key  key = skuPrice:skuId value = price;
        //  还需要获取到方法传递的参数
        Object[] args = joinPoint.getArgs();
        //  拼接缓存key
        String key = prefix+ Arrays.asList(args).toString();
        //  根据缓存的key 获取缓存的数据
        //  key 缓存的key signature 能够获取到方法上具体的返回值
        // Class returnType = signature.getReturnType();
        try {
            object = cacheHit(key,signature);
            //  对其进行判断
            if (object==null){
                // 说明缓存中并没有数据，从数据库中获取数据
                String lockKey = prefix+":lock";
                RLock lock = redissonClient.getLock(lockKey);
                //  尝试加锁，
                boolean result = lock.tryLock(RedisConst.SKULOCK_EXPIRE_PX1, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);

                // 判断是否加锁成功
                if (result){
                    try {
                        // 查询数据库  实现类中获取 skuInfo = getSkuInfoDB(skuId);
                        // 表示执行带有@GmallCache 注解的方法
                        object = joinPoint.proceed(joinPoint.getArgs());

                        //  判断object  防止缓存穿透
                        if (object==null){
                            Object object1 = new Object();
                            redisTemplate.opsForValue().set(key, JSON.toJSONString(object1),RedisConst.SKUKEY_TEMPORARY_TIMEOUT,TimeUnit.SECONDS);
                            return object1;
                        }
                        // 如果object 不为空
                        redisTemplate.opsForValue().set(key, JSON.toJSONString(object),RedisConst.SKUKEY_TIMEOUT,TimeUnit.SECONDS);
                        return object;
                    } finally {
                        //  解锁
                        lock.unlock();
                    }
                }else {
                    //  没有获取到锁，自旋
                    Thread.sleep(1000);
                    return cacheAround(joinPoint);
                }
            }else {
                return object;
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        //  数据库进行兜底
        return joinPoint.proceed(joinPoint.getArgs());
    }

    //  表示获取缓存的数据
    private Object cacheHit(String key, MethodSignature signature) {
        String strJson = (String) redisTemplate.opsForValue().get(key);
        //  获取到了缓存数据
        if (!StringUtils.isEmpty(strJson)){
            // 此时返回数据是需要数据类型的
            Class returnType = signature.getReturnType();
            //  将字符串转换为对应的数据类型。
            return JSON.parseObject(strJson,returnType);
        }
        return null;
    }


}
