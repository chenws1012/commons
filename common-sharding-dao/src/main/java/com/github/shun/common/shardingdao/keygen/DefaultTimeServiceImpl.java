package com.github.shun.common.shardingdao.keygen;

/**
 * Created by chenwenshun@gmail.com on 2022/7/13
 */
public class DefaultTimeServiceImpl implements TimeService {
    @Override
    public long getCurrentMillis() {
        return System.currentTimeMillis();
    }
}
