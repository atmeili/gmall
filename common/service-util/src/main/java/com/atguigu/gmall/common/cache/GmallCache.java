package com.atguigu.gmall.common.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author mqx
 * @date 2021-2-19 14:31:22
 */
@Target({ElementType.METHOD}) // 注解使用的范围 是方法上
@Retention(RetentionPolicy.RUNTIME) // 表示的是注解的声明周期
public @interface GmallCache {
    // 添加一个属性：组成缓存key 的前缀
    String prefix() default "cache";
}
