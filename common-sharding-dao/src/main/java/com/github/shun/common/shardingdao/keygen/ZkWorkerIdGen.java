package com.github.shun.common.shardingdao.keygen;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

import java.nio.charset.StandardCharsets;
import java.util.Random;

/**
 * @author chenwenshun@gmail.com on 2023/9/18
 */
@Slf4j
public class ZkWorkerIdGen implements WorkerIdGen {

    private CuratorFramework client;

    private String appName;

    public ZkWorkerIdGen(CuratorFramework client, String appName) {
        this.client = client;
        this.appName = appName;
    }

    @Override
    @SneakyThrows
    public Long getWorkerId() {
        boolean flag = false;
        long workId = 0;
        for (int i = 1; i < 1001; i++) {
            try {
                client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath("/shwoody/workId/"+i, appName.getBytes(StandardCharsets.UTF_8));
                client.getConnectionStateListenable().addListener(new ZkConnectionStateListener("/shwoody/workId/"+i, appName.getBytes(StandardCharsets.UTF_8)));
                flag = true;
                workId = i;
                break;
            }catch (KeeperException.NodeExistsException e){
                log.warn("workId is exist, retrying...");
            }

        }

        if (!flag){
            throw new RuntimeException("get workId failed After retrying 20 times！ ");
        }

        log.info("start get workId:{}", workId);
        return workId;
    }

}
