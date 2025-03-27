package com.github.shun.common.ratelimiter;

import com.github.shun.common.ratelimiter.annotation.RateLimiter;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by chenwenshun@gmail.com on 2024/11/22
 */
@Aspect
@RequiredArgsConstructor
@Slf4j
public class RateLimiterAspect {

    private final StringRedisTemplate redisTemplate;
    // Lua 脚本
    private final RedisScript<List<Long>> redisScript;

    private final Cache<String, Integer> refillRateCache = CacheBuilder.newBuilder()
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .maximumSize(200) // Maximum cache size (adjust as needed)
            .build();

    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(
            8,//corePoolSize
            16,//maximumPoolSize
            1000,//keepAliveTime
            TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<Runnable>(5000),//workQueue
            new ThreadFactory() { //新线程创建工厂
                private final AtomicInteger nextId = new AtomicInteger(1);
                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "rateLimiter-thread-" + nextId.getAndIncrement());
                }
            },
            new ThreadPoolExecutor.AbortPolicy()//拒绝策略
    );

    @Around("@annotation(rateLimiter)")
    public Object rateLimiter(ProceedingJoinPoint joinPoint, RateLimiter rateLimiter) throws Throwable {
        // 获取注解参数
        int replenishRate = rateLimiter.replenishRate();
//        int requestedTokens = rateLimiter.requestedTokens();
        String rateLimiterKey = rateLimiter.key();
        if (Objects.isNull(rateLimiterKey)){
            rateLimiterKey = joinPoint.getSignature().toString();
        }

        // 获取动态 refillRate
        // 从缓存中获取 refillRate
        String refillRateKey = "refillRate:"+ rateLimiterKey;
        Integer refillRate = refillRateCache.getIfPresent(refillRateKey);
        if (refillRate == null) {
            // 缓存未命中，查询 Redis
            String refillRateStr =
                    executeWithTimeout(() -> redisTemplate.opsForValue().get(refillRateKey), 1000, TimeUnit.MILLISECONDS);

            try {
                refillRate = (refillRateStr != null) ? Integer.parseInt(refillRateStr) : replenishRate; // Default to annotation value.
            } catch (NumberFormatException e) {
                log.error("Invalid refillRate value in Redis for key: {}, value: {}", refillRateKey, refillRateStr, e);
                refillRate = replenishRate; // Fallback to annotation value
            }
            refillRateCache.put(refillRateKey, refillRate);
        }
        int burstCapacity = (int) Math.ceil(refillRate * 1.2);


        // Redis key
        List<String> keys = getKeys(rateLimiterKey);
        String[] scriptArgs = Arrays.asList(refillRate + "", burstCapacity + "", Instant.now().getEpochSecond() + "",  "1").toArray(new String[0]);


        // 执行 Lua 脚本
        List<Long> results =
                executeWithTimeout(() ->
                        redisTemplate.execute(redisScript, keys, scriptArgs), 1000, TimeUnit.MILLISECONDS);

        if(results == null){
            //降级处理
            return joinPoint.proceed();
        }

        // 限流结果判断
        boolean allowed = (Long)results.get(0) == 1L;
        Long tokensLeft = (Long)results.get(1);
        if (!allowed) {
            throw new RuntimeException("too many request");
        }

        // 执行方法
        return joinPoint.proceed();
    }

    private List<String>getKeys(String id){
        String prefix = "woody_rate_limiter.{" + id;
        String tokenKey = prefix + "}.tokens";
        String timestampKey = prefix + "}.timestamp";
        return Arrays.asList(tokenKey, timestampKey);
    }

    private <T> T executeWithTimeout(Callable<T> task, long timeout, TimeUnit unit) {
        if (task != null) {
            Future<T> future = executor.submit(task);
            try {
                return future.get(timeout, unit);
            } catch (TimeoutException e) {
                future.cancel(true);
                log.error("Task execution timed out", e);
            } catch (Exception e) {
                log.error("Error executing task", e);
            }
       }
        return null;
    }

    public static void main(String[] args) {
        System.out.println((int) Math.ceil(10 * 1.2));
    }
}
