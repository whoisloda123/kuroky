package com.liucan.kuroky.distribution;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

import java.util.Collections;

/**
 * 二.redis分布式锁需要注意的事项
 *  1.加锁
 *      正确：set（key, value, nx, px, time）
 *      错误：setnx和setex执行
 *      原因：没有保证2条指令的原子性，setnx后redis挂了，造成锁永远也解锁不了
 *  2.解锁
 *      正确：执行lua脚本语句，if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end
 *      错误：判断key的value和期望的比较相等后，然后在删除key
 *      原因：没有保证2条指令的原子性，在判断value相等后，刚好key过期然后其他地方获取到锁设置了新的value，然后导致删除了其他的锁
 *  3.redis分布式锁问题
 *      a.会一直自旋，耗cpu性能
 *      b.主从模式下，如果获取到锁后主挂了，从变成主了，其他获取到锁了，而之前的主被拉起来后也有锁，相对于有2个锁了，而zk分布式锁没有这个问题（强一致性），但是性能没有redis高
 */
@Slf4j
public class RedisDistributedLock {
    private static final Long RELEASE_SUCCESS = 1L;

    /**
     * 尝试获取分布式锁
     *
     * @param jedis       Redis客户端
     * @param lockKey     锁
     * @param randomValue 请求随机值
     * @param expireTime  超期时间
     * @return 是否获取成功
     */
    public static boolean tryDistributedLock(Jedis jedis, String lockKey, String randomValue, int expireTime) {
        boolean bLock = false;
        try {
            String result = jedis.set(lockKey, randomValue, "nx", "px", expireTime);
            if (result.equals("ok")) {
                bLock = true;
            }
        } catch (Exception e) {
            log.error("[分布式锁]尝试获取分布式锁异常，lockKey：{},randomValue:{}", lockKey, randomValue, e);
        }
        return bLock;
    }

    /**
     * 获取分布式锁
     *
     * @param jedis       Redis客户端
     * @param lockKey     锁
     * @param randomValue 请求随机值
     * @param expireTime  超期时间
     * @return 是否获取成功
     */
    public static void DistributedLock(Jedis jedis, String lockKey, String randomValue, int expireTime) {
        while (!RedisDistributedLock.tryDistributedLock(jedis, lockKey, randomValue, expireTime)) ;
    }

    /**
     * 释放分布式锁
     *
     * @param jedis       Redis客户端
     * @param lockKey     锁
     * @param randomValue 请求标识
     * @return 是否释放成功
     */
    public static boolean DistributedUnlock(Jedis jedis, String lockKey, String randomValue) {
        boolean bResult = false;
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Object result = jedis.eval(script, Collections.singletonList(lockKey), Collections.singletonList(randomValue));
        if (RELEASE_SUCCESS.equals(result)) {
            bResult = true;
        }
        return bResult;
    }
}
