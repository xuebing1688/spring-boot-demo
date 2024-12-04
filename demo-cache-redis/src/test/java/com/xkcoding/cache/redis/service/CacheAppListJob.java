package com.xkcoding.cache.redis.service;

/**
 * @ClassName: $
 * @Description:
 * @Author: summer
 * @Date: 2024-11-29 18:15
 * @Version: 1.0
 **/


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CacheAppListJob {

  // @Resource
  // RedisUtil redisUtil;

  // 测试把master最新修改rebase到fengxb_01
  // 测试把master最新修改rebase到_01


    /*@PostConstruct
    public void init() {
        int retryCount = 3;
        while (retryCount > 0) {
            if (redisUtil.getRedisLocker().tryLock(RedisKeyConstants.CHAIN_CONFIG_LOCK, TimeUnit.MINUTES, 0, 5)) {
                try {
                    int saveTotal = commonService.saveAppInfo();
                    log.info("一共更新了{}条应用数据", saveTotal);
                } finally {
                    redisUtil.getRedisLocker().unlock(RedisKeyConstants.CHAIN_CONFIG_LOCK);
                }
                return;
            }
            retryCount--;
            try {
                Thread.sleep(1000); // 重试间隔1秒
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        log.warn("未能获取分布式锁，跳过任务执行");
    }*/

    /*@Scheduled(cron = "0 0 * * * ?")
    public void  saveAppInfo() {
      //争抢分布式锁
      try {
        if (!redisUtil.getRedisLocker().tryLock(RedisKeyConstants.CHAIN_CONFIG_LOCK, TimeUnit.MINUTES, 0, 5)) {
          return;
        }
        int saveTotal = commonService.saveAppInfo();
        log.info("一共更新了{}条应用数据", saveTotal);
      } finally {
        //释放资源
        redisUtil.getRedisLocker().unlock(RedisKeyConstants.CHAIN_CONFIG_LOCK);
      }
    }*/

}
