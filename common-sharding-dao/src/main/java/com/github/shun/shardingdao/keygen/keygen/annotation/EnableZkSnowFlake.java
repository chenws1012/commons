package com.github.shun.shardingdao.keygen.keygen.annotation;

import com.github.shun.shardingdao.keygen.keygen.ZkSnowFlakeConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author chenwenshun@gmail.com on 2023/9/18
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({ZkSnowFlakeConfig.class})
@Documented
public @interface EnableZkSnowFlake {
}
