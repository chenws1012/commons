package com.github.shun.common.ratelimiter.annotation;

import com.github.shun.common.ratelimiter.RateLimiterConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Created by chenwenshun@gmail.com on 2024/11/22
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({RateLimiterConfig.class})
@Documented
public @interface EnableRateLimiter {
}
