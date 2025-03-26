package com.github.shun.shardingdao.keygen.keygen;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

/**
 * @author: chenwenshun@gmail.com
 * @description: 监听zookeeper连接状态，重连后重新注册workId
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
            while(true) {
                try {
                    if (client.getZookeeperClient().blockUntilConnectedOrTimedOut()) {
                        //连接恢复，重新注册workId
                        client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path, data);
                        log.info("recreate workId:" + path);
                        break;
                    }
                } catch (KeeperException.NodeExistsException e) {
                    log.info("Znode " + path + " already exists, this might be caused by a delete delay from the zk server");
                    try {
                        //session过期，如果workId还存在，可能是zk server删除的延迟，这里手动删除，用新的session重新创建，保持心跳
                        client.delete().forPath(path);
                        log.info("deleted node {}", path);
                    } catch (Exception ex) {
                        log.warn("delete node {} Exception {}", path, ex.getMessage());
                    }
                } catch (InterruptedException e) {
                    log.error("InterruptedException", e);
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("recreate workId Exception", e);
                    break;
                }
            }
        }

    }
}
