package com.example.ice;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



/**
 * @ClassName:  CompletableFutureTest
 * @Description: CompletableFuture 的使用示例
 * @Author: summer
 * @Date: 2024-09-26 12:48
 * @Version: 1.0
 **/
public class CompletableFutureTest {

  // 假设这是你的数据库连接信息
  private static final String URL = "jdbc:mysql://localhost:3306/yourdatabase";
  private static final String USER = "yourusername";
  private static final String PASSWORD = "yourpassword";

  // 模拟的 NetList 类
  public static class NetList {
    // NetList 的属性和方法
  }

  // 并行查询数据库的方法
  public List<NetList> parallelDatabaseQuery(String query1, String query2) {
    // 创建一个固定大小的线程池
    ExecutorService executor = Executors.newFixedThreadPool(2);

    // 使用 CompletableFuture 来执行异步查询
    CompletableFuture<NetList> future1 = CompletableFuture.supplyAsync(() -> {
      try {
        return executeQuery(query1);
      } catch (SQLException e) {
        e.printStackTrace();
        return null;
      }
    }, executor);

    CompletableFuture<NetList> future2 = CompletableFuture.supplyAsync(() -> {
      try {
        return executeQuery(query2);
      } catch (SQLException e) {
        e.printStackTrace();
        return null;
      }
    }, executor);

    // 等待所有异步任务完成，并合并结果
    CompletableFuture<List<NetList>> combinedFuture = CompletableFuture.allOf(future1, future2)
      .thenApply(v -> {
        List<NetList> combinedList = new ArrayList<>();
        combinedList.add(future1.join());
        combinedList.add(future2.join());
        return combinedList;
      });

    // 关闭线程池
    combinedFuture.whenComplete((v, t) -> executor.shutdown());

    // 获取并行查询的结果
    return combinedFuture.join();
  }

  // 执行单个查询的方法
  private NetList executeQuery(String query) throws SQLException {
    // 建立数据库连接
    try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
         PreparedStatement pstmt = conn.prepareStatement(query);
         ResultSet rs = pstmt.executeQuery()) {

      // 处理查询结果集
      while (rs.next()) {
        // 假设这里你已经解析了结果集并创建了 NetList 对象
        NetList netList = new NetList();
        // 设置 NetList 对象的属性
        // ...
        return netList;
      }
    }
    // 如果查询没有结果，返回 null 或者抛出异常
    return null;
  }

  public static void main(String[] args) {
    CompletableFutureTest databaseQuery = new CompletableFutureTest();
    List<NetList> result = databaseQuery.parallelDatabaseQuery("SELECT * FROM table1", "SELECT * FROM table2");
    // 处理结果
  }
}
