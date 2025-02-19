package com.example.common.service;

import com.example.common.config.ClickHouseManager;
import com.example.common.entity.Constant;
import com.example.common.entity.KafkaRecord;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.sql.SQLException;
/**
 * @description:  通过读取文件 解析Kafka二进制数据
 * @date: 2025/1/3 16:18
 * @param  *   null
 * @return null
 **/

public class KafkaBinaryParserToDB {


  /**
   * @description:  通过读取文件 解析Kafka二进制数据
   * @date: 2025/1/3 16:18
   * @param *   null
   * @return null
   **/

    // 定义 KafkaTransBinaryHead 类，匹配 Go 的结构体
    static class KafkaTransBinaryHead {
      byte magic;
      byte headerLen;
      byte ipVersion;
      byte reserve;
      boolean isFirst;
      boolean isDone;
      byte netlinkID;
      long dateLen; // 使用 long 以容纳 uint32
      int sequence;  // 使用 int 以容纳 uint16
      int pktCount;  // 使用 int 以容纳 uint16
      byte[] ipAddr;

      @Override
      public String toString() {
        return String.format(
          "headlen: %d, ipVersion: %d, reserve: %d, isFirst: %b, isDone: %b, netlinkId: %d, dataLen: %d, seq: %d, pktCount: %d",
          headerLen & 0xFF, ipVersion & 0xFF, reserve & 0xFF, isFirst, isDone,
          netlinkID & 0xFF, dateLen, sequence & 0xFFFF, pktCount & 0xFFFF);
      }
    }

    // 定义 TestField 类
    static class TestField {
      int type;
      String name;

      TestField(int type, String name) {
        this.type = type;
        this.name = name;
      }
    }

    // 全局数据缓冲区
    static ByteArrayOutputStream s_data = new ByteArrayOutputStream(800 * 1024 * 1024); // 800MB
    static long s_dataLen = 0;

    // 解析 KafkaTransBinaryHead 从字节数组中
    public static KafkaTransBinaryHead parseKafkaTransBinaryHead(byte[] data) throws Exception {
      if (data.length < 12) { // 1+1+1+1+4+2+2 = 12 字节
        throw new Exception("数据长度不足以解析 KafkaTransBinaryHead");
      }

      KafkaTransBinaryHead head = new KafkaTransBinaryHead();
      head.magic = data[0];
      head.headerLen = data[1];

      // 解析 IPVersion, Reserve, IsFirst, IsDone
      byte ipVersionAndFlags = data[2];
      head.ipVersion = (byte) (ipVersionAndFlags & 0x0F);
      head.reserve = (byte) ((ipVersionAndFlags >> 4) & 0x03);
      head.isFirst = ((ipVersionAndFlags >> 6) & 0x01) == 1;
      head.isDone = ((ipVersionAndFlags >> 7) & 0x01) == 1;

      head.netlinkID = data[3];

      // 使用大端序解析 DateLen, Sequence, PktCount
      ByteBuffer buffer = ByteBuffer.wrap(data, 4, 8).order(java.nio.ByteOrder.BIG_ENDIAN);
      head.dateLen = Integer.toUnsignedLong(buffer.getInt());
      head.sequence = Short.toUnsignedInt(buffer.getShort());
      head.pktCount = Short.toUnsignedInt(buffer.getShort());

      // 计算 IPAddr 的长度
      int fixedSize = 12; // 1 + 1 + 1 + 1 + 4 + 2 + 2
      int ipAddrLen = (int) head.headerLen - fixedSize;
      if (ipAddrLen > 0) {
        if (data.length < fixedSize + ipAddrLen) {
          throw new Exception("数据长度不足以解析 IPAddr");
        }
        head.ipAddr = new byte[ipAddrLen];
        System.arraycopy(data, fixedSize, head.ipAddr, 0, ipAddrLen);
      } else {
        head.ipAddr = new byte[0];
      }

      return head;
    }

    // 实现类似 C++ 的 AppendFormat，使用 StringBuilder
    static class CustomStringBuilder {
      private StringBuilder builder;

      CustomStringBuilder() {
        builder = new StringBuilder();
      }

      void appendFormat(String format, Object... args) {
        builder.append(String.format(format, args));
      }

      @Override
      public String toString() {
        return builder.toString();
      }
    }

    // 打印数据，以十六进制和 ASCII 格式显示
    public static void printData(String label, byte[] data, int length) {
      CustomStringBuilder sb = new CustomStringBuilder();
      sb.appendFormat("%s[%d]:\n", label, length);

      for (int i = 0; i < length; i += 16) {
        sb.appendFormat("| ");
        // 打印十六进制部分
        for (int j = i; j < i + 16 && j < length; j++) {
          sb.appendFormat("%02x ", data[j]);
        }
        // 填充空格以对齐
        for (int j = i + 16 - length; j > 0 && j < 16; j++) {
          sb.appendFormat("   ");
        }
        sb.appendFormat("|");
        // 打印 ASCII 部分
        for (int j = i; j < i + 16 && j < length; j++) {
          char c = (char) data[j];
          if (!Character.isISOControl(c) && c != '\t') {
            sb.appendFormat("%c", c);
          } else {
            sb.appendFormat(".");
          }
        }
        // 填充空格以对齐
        for (int j = i + 16 - length; j > 0 && j < 16; j++) {
          sb.appendFormat(" ");
        }
        sb.appendFormat("|\n");
      }

      System.out.print(sb.toString());
    }

    // 读取文件内容，返回字节数组
    public static byte[] readFile(String fullPath, long limitSize) throws IOException {
      FileInputStream fis = new FileInputStream(fullPath);
      try {
        // 获取文件大小
        long fileSize = fis.getChannel().size();

        // 检查大小限制
        if (limitSize != 0 && fileSize > limitSize) {
          throw new IOException("文件大小超过限制");
        }

        // 读取文件内容
        byte[] content = new byte[(int) fileSize];
        int bytesRead = fis.read(content);
        if (bytesRead != fileSize) {
          throw new IOException("无法读取完整文件内容");
        }

        // 在内容前添加文件大小（4 字节，大端序）和1字节0
        ByteBuffer resultBuffer = ByteBuffer.allocate(content.length + 5).order(java.nio.ByteOrder.BIG_ENDIAN);
        resultBuffer.putInt((int) fileSize);
        resultBuffer.put(content);
        resultBuffer.put((byte) 0);
        return resultBuffer.array();
      } finally {
        fis.close();
      }
    }

    // 打印头部信息
    public static void printHead(byte[] data) {
      try {
        KafkaTransBinaryHead head = parseKafkaTransBinaryHead(data);
        // 获取 taskId，假设在头部之后的 2 字节位置
        if (data.length < head.headerLen + 2) {
          System.out.println("数据不足以解析 taskId");
          return;
        }
        ByteBuffer buffer = ByteBuffer.wrap(data, (int) head.headerLen, 2).order(java.nio.ByteOrder.BIG_ENDIAN);
        int taskId = Short.toUnsignedInt(buffer.getShort());

        System.out.printf("headlen: %d, ipVersion: %d, reserve: %d, isFirst: %b, isDone: %b, netlinkId: %d, dataLen: %d, seq: %d, pktCount: %d, taskId: %d\n",
          head.headerLen & 0xFF, head.ipVersion & 0xFF, head.reserve & 0xFF, head.isFirst, head.isDone,
          head.netlinkID & 0xFF, head.dateLen, head.sequence, head.pktCount, taskId);
      } catch (Exception e) {
        System.out.printf("解析头部失败: %v\n", e.getMessage());
      }
    }

    // 处理数据，将数据复制到全局缓冲区
    public static int procData(byte[] data) {
      try {
        KafkaTransBinaryHead head = parseKafkaTransBinaryHead(data);
        long dataLen = head.dateLen;
        System.out.printf("proc %d %d\n", dataLen, s_dataLen);

        // 计算需要复制的数据偏移量和长度
        int copyOffset = (int) head.headerLen + 2; // headerLen +2 字节 taskId
        int copyLength = (int) dataLen - 2;
        if (copyOffset + copyLength > data.length) {
          System.out.println("数据不足以复制");
          return 0;
        }
        s_data.write(data, copyOffset, copyLength);
        s_dataLen += copyLength;

        return copyOffset + copyLength;
      } catch (Exception e) {
        System.out.printf("解析头部失败: %v\n", e.getMessage());
        return 0;
      }
    }

    public static void main(String[] args) {
      try {
        // 读取文件 "kafka二进制.log"
        byte[] content = readFile(Constant.KAFKA_LOG_Log, 0);

        if (content.length < 4) {
          System.out.println("文件内容不足以读取 bufferLen");
          return;
        }

        // 获取 bufferLen
        ByteBuffer buffer = ByteBuffer.wrap(content, 0, 4).order(java.nio.ByteOrder.BIG_ENDIAN);
        long bufferLen = Integer.toUnsignedLong(buffer.getInt());
        byte[] data = new byte[(int) bufferLen];
        System.arraycopy(content, 4, data, 0, (int) bufferLen);
        int pos = 0;

        // 为避免 bufferLen 超出实际数据长度，调整 bufferLen
        if (bufferLen > data.length) {
          System.out.printf("警告: bufferLen (%d) 超过实际数据长度 (%d)，将调整为实际数据长度。\n", bufferLen, data.length);
          bufferLen = data.length;
        }

        while (pos < bufferLen) {
          if (pos + 12 > data.length) { // 至少需要 12 字节解析头部
            System.out.println("当前位置超过数据长度");
            break;
          }
          // 打印头部信息
          printHead(data, pos);
          // 处理数据
          int lenProcessed = procData(data, pos);
          if (lenProcessed == 0) {
            System.out.println("处理数据时出错，退出循环");
            break;
          }
          // 打印前 16 字节数据
          int end = pos + 16;
          if (end > data.length) {
            end = data.length;
          }
          printData("data1", data, end - pos);
          System.out.printf("%d %d\n", bufferLen, pos);
          pos += lenProcessed + 1;
        }

        // 解析 s_data
        if (s_dataLen > 0) {
          byte[] s_data_bytes = s_data.toByteArray();
          pos = 0;
          List<TestField> fieldT = new ArrayList<>();

          // 读取字段数量
          if (pos + 2 > s_data_bytes.length) {
            System.out.println("s_data 数据不足以读取字段数量");
            return;
          }
          ByteBuffer fieldBuffer = ByteBuffer.wrap(s_data_bytes, pos, 2).order(java.nio.ByteOrder.BIG_ENDIAN);
          int fieldCount = Short.toUnsignedInt(fieldBuffer.getShort());
          pos += 2;

          System.out.printf("fieldCount = %d\n", fieldCount);

          // 读取每个字段的信息
          for (int i = 0; i < fieldCount; i++) {
            if (pos + 4 > s_data_bytes.length) {
              System.out.println("s_data 数据不足以读取字段长度");
              break;
            }
            ByteBuffer lenBuffer = ByteBuffer.wrap(s_data_bytes, pos, 4).order(java.nio.ByteOrder.BIG_ENDIAN);
            long lenField = Integer.toUnsignedLong(lenBuffer.getInt());
            pos += 4;

            if (pos + lenField > s_data_bytes.length) {
              System.out.println("s_data 数据不足以读取字段名称");
              break;
            }
            String fieldName = new String(s_data_bytes, pos, (int) lenField, StandardCharsets.UTF_8);
            pos += lenField;

            if (pos + 1 > s_data_bytes.length) {
              System.out.println("s_data 数据不足以读取字段类型");
              break;
            }
            int fieldType = Byte.toUnsignedInt(s_data_bytes[pos]);
            pos += 1;

            System.out.printf("field: %s %d %d\n", fieldName, lenField, fieldType);
            fieldT.add(new TestField(fieldType, fieldName));
          }
          System.out.println();

          // 读取 netlinkCount, netlinkId, netlinkRecord, netlinkTime, teimRecord
          if (pos + 1 + 2 + 4 + 8 + 4 > s_data_bytes.length) {
            System.out.println("s_data 数据不足以读取 netlink 和记录信息");
            return;
          }

          int netlinkCount = Byte.toUnsignedInt(s_data_bytes[pos]);
          pos += 1;

          ByteBuffer netlinkIdBuffer = ByteBuffer.wrap(s_data_bytes, pos, 2).order(java.nio.ByteOrder.BIG_ENDIAN);
          int netlinkId = Short.toUnsignedInt(netlinkIdBuffer.getShort());
          pos += 2;

          ByteBuffer netlinkRecordBuffer = ByteBuffer.wrap(s_data_bytes, pos, 4).order(java.nio.ByteOrder.BIG_ENDIAN);
          long netlinkRecord = Integer.toUnsignedLong(netlinkRecordBuffer.getInt());
          pos += 4;

          ByteBuffer netlinkTimeBuffer = ByteBuffer.wrap(s_data_bytes, pos, 8).order(java.nio.ByteOrder.BIG_ENDIAN);
          long netlinkTime = netlinkTimeBuffer.getLong();
          pos += 8;

          ByteBuffer teimRecordBuffer = ByteBuffer.wrap(s_data_bytes, pos, 4).order(java.nio.ByteOrder.BIG_ENDIAN);
          long teimRecord = Integer.toUnsignedLong(teimRecordBuffer.getInt());
          pos += 4;

          System.out.printf("%d %d %d %d %d\n", netlinkCount, netlinkId, netlinkRecord, netlinkTime, teimRecord);

          // 在处理记录的循环中收集数据
          List<KafkaRecord> records = new ArrayList<>();

          // 处理每条记录
          for (long i = 0; i < teimRecord && pos < s_dataLen; i++) {
            KafkaRecord record = new KafkaRecord();
            record.setNetlinkTime(netlinkTime);
            record.setNetlinkId(netlinkId);

            for (TestField f : fieldT) {
              Object value = null;
              switch (f.type) {
                case 1:
                case 12:
                  value = s_data_bytes[pos];
                  pos += 1;
                  break;
                case 2:
                  value = Short.toUnsignedInt(ByteBuffer.wrap(s_data_bytes, pos, 2).getShort());
                  pos += 2;
                  break;
                // ... 其他case处理保持不变，但要设置value ...
              }
              if (value != null) {
                record.addField(f.name, value);
              }
            }
            records.add(record);
          }

          // 批量插入数据到ClickHouse
          try {
            ClickHouseManager.batchInsertRecords(records);
            System.out.println("Successfully inserted " + records.size() + " records into ClickHouse");
          } catch (SQLException e) {
            System.err.println("Failed to insert records into ClickHouse: " + e.getMessage());
            e.printStackTrace();
          }

          System.out.printf("%d %d %d\n", teimRecord, pos, s_dataLen);
        }

        System.out.println("进程 已完成，退出代码为 0");
      } catch (Exception e) {
        System.out.printf("读取文件失败: %v\n", e.getMessage());
      }
    }

    // 修改 printHead 方法以接受偏移量
    public static void printHead(byte[] data, int offset) {
      try {
        KafkaTransBinaryHead head = parseKafkaTransBinaryHead(sliceArray(data, offset, data.length - offset));
        // 获取 taskId，假设在头部之后的 2 字节位置
        if (data.length < offset + head.headerLen + 2) {
          System.out.println("数据不足以解析 taskId");
          return;
        }
        ByteBuffer buffer = ByteBuffer.wrap(data, offset + (int) head.headerLen, 2).order(java.nio.ByteOrder.BIG_ENDIAN);
        int taskId = Short.toUnsignedInt(buffer.getShort());

        System.out.printf("headlen: %d, ipVersion: %d, reserve: %d, isFirst: %b, isDone: %b, netlinkId: %d, dataLen: %d, seq: %d, pktCount: %d, taskId: %d\n",
          head.headerLen & 0xFF, head.ipVersion & 0xFF, head.reserve & 0xFF, head.isFirst, head.isDone,
          head.netlinkID & 0xFF, head.dateLen, head.sequence, head.pktCount, taskId);
      } catch (Exception e) {
        System.out.printf("解析头部失败: %s\n", e.getMessage());
      }
    }

    // 修改 procData 方法以接受偏移量
    public static int procData(byte[] data, int offset) {
      try {
        KafkaTransBinaryHead head = parseKafkaTransBinaryHead(sliceArray(data, offset, data.length - offset));
        long dataLen = head.dateLen;
        System.out.printf("proc %d %d\n", dataLen, s_dataLen);

        // 计算需要复制的数据偏移量和长度
        int copyOffset = (int) head.headerLen + 2; // headerLen +2 字节 taskId
        int copyLength = (int) dataLen - 2;
        if (copyOffset + copyLength > data.length - offset) {
          System.out.println("数据不足以复制");
          return 0;
        }
        s_data.write(data, offset + copyOffset, copyLength);
        s_dataLen += copyLength;

        return copyOffset + copyLength;
      } catch (Exception e) {
        System.out.printf("解析头部失败: %s\n", e.getMessage());
        return 0;
      }
    }

    // 辅助方法：切割字节数组
    public static byte[] sliceArray(byte[] src, int offset, int length) {
      byte[] dest = new byte[length];
      System.arraycopy(src, offset, dest, 0, length);
      return dest;
    }
  }
