package com.github.shun.common.shardingdao.keygen;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * @author chenwenshun@gmail.com on 2023/9/18
 */
public class ZkSnowFlakeConfig {

    @Value("${zk.addresses}")
    private String addresses;

    @Value("${zk.sessionTimeoutMs}")
    private int sessionTimeoutMs;

    @Value("${zk.connectionTimeoutMs}")
    private int connectionTimeoutMs;

    @Value("${spring.application.name:}")
    private String appName;

    @Bean(initMethod = "start")
    public CuratorFramework curatorFramework(RetryPolicy retryPolicy) {
        return CuratorFrameworkFactory.newClient(
                addresses, sessionTimeoutMs, connectionTimeoutMs, retryPolicy);
    }


    @Bean
    public RetryPolicy retryPolicy(){
       return new ExponentialBackoffRetry(1000, 5);
    }

    @Bean
    @ConditionalOnMissingBean
    public SnowflakeKeyGenerator snowflakeKeyGenerator(CuratorFramework curatorFramework){
        ZkWorkerIdGen zkWorkerIdGen = new ZkWorkerIdGen(curatorFramework, appName);
        SnowflakeKeyGenerator snowflakeKeyGenerator = new SnowflakeKeyGenerator();
        snowflakeKeyGenerator.setWorkerIdGen(zkWorkerIdGen);
        snowflakeKeyGenerator.init();
        return snowflakeKeyGenerator;
    }

}
