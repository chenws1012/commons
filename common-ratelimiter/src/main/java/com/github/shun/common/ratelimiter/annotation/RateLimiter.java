package com.github.shun.common.ratelimiter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by chenwenshun@gmail.com on 2024/11/22
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimiter {
    String key() default "";
    int replenishRate(); // 每秒填充的令牌数
//    int burstCapacity();  // 最大令牌数
//    int requestedTokens() default 1; // 每次请求的令牌数
}
