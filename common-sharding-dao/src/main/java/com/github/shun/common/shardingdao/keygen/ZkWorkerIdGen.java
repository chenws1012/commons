package com.github.shun.common.shardingdao.keygen;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author chenwenshun@gmail.com on 2023/9/18
 */
@Slf4j
public class ZkWorkerIdGen implements WorkerIdGen {

    private static final int WORK_ID_BITS = 10; // workId 的位数
    private static final int MAX_WORK_ID = (1 << WORK_ID_BITS) - 1; // 最大 workId 值

    private static final String ZK_ROOT_PATH = "/snowflake/workId";

    private CuratorFramework client;

    private String appName;

    public ZkWorkerIdGen(CuratorFramework client, String appName) {
        this.client = client;
        this.appName = appName;
    }

    @Override
    @SneakyThrows
    public Long getWorkerId() {
        List<String> existingWorkIds = Collections.emptyList();
        try {
            existingWorkIds = client.getChildren().forPath(ZK_ROOT_PATH);
        } catch (KeeperException.NoNodeException e) {
            log.warn("Path {} does not exist.", ZK_ROOT_PATH);
        } catch (KeeperException e) {
            log.error("Failed to get children from ZooKeeper", e);
        }

        Set<Integer> existingWorkIdSet = new HashSet<>();
        for (String id : existingWorkIds) {
            existingWorkIdSet.add(Integer.parseInt(id));
        }

        boolean flag = false;
        long workId = 0;
        List<Integer> candidates = IntStream.range(1, MAX_WORK_ID).boxed().collect(Collectors.toList());
        Collections.shuffle(candidates);
        for (int i : candidates) {
            if (existingWorkIdSet.contains(i)) {
                continue;
            }
            try {
                client.create().creatingParentsIfNeeded()
                        .withMode(CreateMode.EPHEMERAL)
                        .forPath(ZK_ROOT_PATH + "/" + i, appName.getBytes(StandardCharsets.UTF_8));
                flag = true;
                workId = i;
                break;
            }catch (KeeperException.NodeExistsException e){
                log.warn("workId is exist, retrying...");
            }

        }

        if (!flag){
            throw new RuntimeException("get workId failed！ ");
        }

        client.getConnectionStateListenable()
                .addListener(new ZkConnectionStateListener(ZK_ROOT_PATH + "/" + workId, appName.getBytes(StandardCharsets.UTF_8)));

        log.info("start get workId:{}", workId);
        return workId;
    }

    public static void main(String[] args) {
        List<String> existingWorkIds = Arrays.asList("1", "2", "4", "5", "6", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25","26", "27", "28", "29", "30");
        Set<Integer> existingWorkIdSet = new HashSet<>();
        for (String id : existingWorkIds) {
            existingWorkIdSet.add(Integer.parseInt(id));
        }
        for (int i = 1; i < 50; i++) {
            if (existingWorkIdSet.contains(i)) {
                System.out.println("is exist!");
                continue;
            }
            System.out.println(i);
        }
    }

}
