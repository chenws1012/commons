package com.github.shun.common.ratelimiter;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

import java.util.List;

/**
 * Created by chenwenshun@gmail.com on 2024/11/22
 */
public class RateLimiterConfig {
    @Bean
    public RedisScript woodyRedisRequestRateLimiterScript() {
        DefaultRedisScript redisScript = new DefaultRedisScript();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("META-INF/scripts/woody_request_rate_limiter.lua")));
        redisScript.setResultType(List.class);
        return redisScript;
    }

    @Bean
    public RateLimiterAspect rateLimiterAspect(StringRedisTemplate redisTemplate, @Qualifier("woodyRedisRequestRateLimiterScript") RedisScript<List<Long>> redisScript) {
        return new RateLimiterAspect(redisTemplate, redisScript);
    }

}
