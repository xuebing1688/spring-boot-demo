package com.example.thread;

/**
 * @ClassName: $
 * @Description:
 * @Author: summer
 * @Date: 2024-12-03 10:32
 * @Version: 1.0
 **/

import java.util.concurrent.CountDownLatch;

public class CountDownLatchExample {

  public static void main(String[] args) throws InterruptedException {
    //  初始化CountDownLatch，设置需要等待的线程数量为3
    CountDownLatch latch = new CountDownLatch(3);

    //  创建并启动三个线程
    for (int i = 0; i < 3; i++) {
      new Thread(() -> {
        try {
          //  模拟任务执行
          Thread.sleep((long) (Math.random() * 10000));
          System.out.println(Thread.currentThread().getName() + "  任务完成");
        } catch (InterruptedException e) {
          e.printStackTrace();
        } finally {
          //  任务完成后，将CountDownLatch的计数减1
          latch.countDown();
        }
      }).start();
    }

    //  等待所有线程完成任务
    latch.await();
    System.out.println("所有任务都已完成，继续执行后续操作");
  }
}
