package com.github.shun.shardingdao.keygen.keygen;

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
        Random random = new Random();
        boolean flag = false;
        long workId = 0;
        int retryTimes = 0;
        while (!flag && retryTimes < 20){
            workId = random.nextInt(1000)+1L;
            try {
                client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath("/shun/workId/"+workId, appName.getBytes(StandardCharsets.UTF_8));
                client.getConnectionStateListenable().addListener(new ZkConnectionStateListener("/shun/workId/"+workId, appName.getBytes(StandardCharsets.UTF_8)));
                flag = true;
            } catch (KeeperException.NodeExistsException e) {
                e.printStackTrace();
            }
            retryTimes++;
        }
        if (!flag){
            throw new RuntimeException("get workId failed After retrying 20 times！ ");
        }

        log.info("start get workId:{}", workId);
        return workId;
    }

}
