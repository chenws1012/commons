package com.github.shun.common.shardingdao.keygen;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

import java.util.concurrent.CompletableFuture;

/**
 * @author: Wilson Chen
 * @description: TODO
 * @date: 2024/5/4 7:32 PM
 * @version: 1.0
 */
@Slf4j
public class ZkConnectionStateListener implements ConnectionStateListener{

    private String path;
    private byte[] data;

    public ZkConnectionStateListener(String path, byte[] data) {
        this.path = path;
        this.data = data;
    }

    @Override
    public void stateChanged(CuratorFramework client, ConnectionState newState) {
        log.info("stateChanged:{}", newState);
        if(newState == ConnectionState.LOST){
            log.info("session has expired");
            CompletableFuture.runAsync(() -> handleSessionLost(client));
        }

    }

    private void handleSessionLost(CuratorFramework client) {
        while (true) {
            try {
                if (client.getZookeeperClient().blockUntilConnectedOrTimedOut()) {
                    try {
                        client.create().creatingParentsIfNeeded()
                                .withMode(CreateMode.EPHEMERAL)
                                .forPath(path, data);
                        log.info("Successfully recreated ephemeral node: {}", path);
                        break;
                    } catch (KeeperException.NodeExistsException e) {
                        log.warn("Node already exists: {}. Attempting to delete and recreate.", path);
                        try {
                            client.delete().forPath(path);
                            log.info("Deleted existing node: {}", path);
                        } catch (Exception ex) {
                            log.warn("Failed to delete node {}: {}", path, ex.getMessage());
                        }
                    }
                }
            } catch (InterruptedException e) {
                log.error("Retry thread interrupted.", e);
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Exception while recreating ephemeral node: {}", path, e);
                break;
            }
        }
    }
}
