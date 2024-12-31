package com.xkcoding.swagger.controller;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class BinaryParserUtils {

  // 定义 Kafka 二进制头结构
  static class KafkaTransBinaryHead {
    byte magic;
    byte headerLen;
    byte ipVersion;
    byte reserve;
    byte isFirst;
    byte isDone;
    byte netlinkId;
    int dateLen;
    short sequence;
    short pktCount;
  }

  // 定义字段结构
  static class Field {
    int type;
    String name;

    Field(int type, String name) {
      this.type = type;
      this.name = name;
    }
  }

  // 读取文件内容
  public static byte[] readFile(String filePath) throws IOException {
    try (FileInputStream fis = new FileInputStream(filePath)) {
      byte[] content = new byte[fis.available()];
      fis.read(content);
      return content;
    }
  }

  // 解析 Kafka 二进制头
  public static KafkaTransBinaryHead parseKafkaHead(ByteBuffer buffer) {
    KafkaTransBinaryHead head = new KafkaTransBinaryHead();
    head.magic = buffer.get();
    head.headerLen = buffer.get();
    byte flags = buffer.get();
    head.ipVersion = (byte) (flags & 0x0F);
    head.reserve = (byte) ((flags >> 4) & 0x03);
    head.isFirst = (byte) ((flags >> 6) & 0x01);
    head.isDone = (byte) ((flags >> 7) & 0x01);
    head.netlinkId = buffer.get();
    head.dateLen = buffer.getInt();
    head.sequence = buffer.getShort();
    head.pktCount = buffer.getShort();
    return head;
  }

  // 打印数据
  public static void printData(String prefix, byte[] data, int len) {
    StringBuilder sb = new StringBuilder();
    sb.append(prefix).append("[").append(len).append("]:\n");

    for (int i = 0; i < len; i += 16) {
      sb.append("| ");
      for (int j = i, k = 0; k < 16 && j < len; j++, k++) {
        sb.append(String.format("%02x ", data[j]));
      }
      for (int k = (len - i) % 16; k < 16; k++) {
        sb.append("   ");
      }
      sb.append("|");
      for (int j = i, k = 0; k < 16 && j < len; j++, k++) {
        char c = (char) data[j];
        if (!Character.isISOControl(c) && !Character.isWhitespace(c)) {
          sb.append(c);
        } else {
          sb.append('.');
        }
      }
      for (int k = (len - i) % 16; k < 16; k++) {
        sb.append(" ");
      }
      sb.append("|\n");
    }

    System.out.println(sb.toString());
  }

  // 主函数
  public static void main(String[] args) {
    try {
      // 读取文件
      byte[] content = readFile("E:\\myfile\\mywork\\lingcloud\\cpic\\2579_1_2020.txt");
      ByteBuffer buffer = ByteBuffer.wrap(content).order(ByteOrder.BIG_ENDIAN);

      // 解析 Kafka 头
      KafkaTransBinaryHead head = parseKafkaHead(buffer);
      System.out.printf("headlen: %d, ipVersion: %d, reserve: %d, isFirst: %d, isDone: %d, netlinkId: %d, dataLen: %d, seq: %d, pktCount: %d\n",
        head.headerLen, head.ipVersion, head.reserve, head.isFirst, head.isDone, head.netlinkId, head.dateLen, head.sequence, head.pktCount);

      // 打印数据
      printData("data1", content, content.length);

      // 处理数据
      byte[] sData = new byte[1024 * 1024 * 4];
      int sDataLen = 0;
      System.arraycopy(content, buffer.position(), sData, sDataLen, head.dateLen - 2);
      sDataLen += head.dateLen - 2;
      System.out.printf("proc %d %d\n", head.dateLen, sDataLen);

      // 解析字段
      List<Field> fieldT = new ArrayList<>();
      int pos = 0;

      buffer.order(ByteOrder.LITTLE_ENDIAN);
      short fieldCountShort = buffer.getShort(pos);
      int fieldCount = Short.toUnsignedInt(fieldCountShort);
      pos += 2;


      System.out.printf("fieldCount = %d\n", fieldCount);
      while (fieldCount > 0) {
        int len = buffer.getInt(pos);
        pos += 4;
        byte[] fieldNameBytes = new byte[len];
        System.arraycopy(sData, pos, fieldNameBytes, 0, len);
        pos += len;
        byte fieldType = sData[pos];
        pos += 1;
        String fieldName = new String(fieldNameBytes);
        fieldT.add(new Field(fieldType, fieldName));
        System.out.printf("field: %s %d %d\n", fieldName, len, fieldType);
        fieldCount--;
      }

      // 解析记录
      byte netlinkCount = sData[pos];
      pos += 1;
      short netlinkId = buffer.getShort(pos);
      pos += 2;
      int netlinkRecord = buffer.getInt(pos);
      pos += 4;
      long netlinkTime = buffer.getLong(pos);
      pos += 8;
      int teimRecord = buffer.getInt(pos);
      pos += 4;

      System.out.printf("%d %d %d %d %d\n", netlinkCount, netlinkId, netlinkRecord, netlinkTime, teimRecord);

      while (teimRecord > 0 && pos < sDataLen) {
        teimRecord--;
        for (Field f : fieldT) {
          System.out.printf("%s %d %d ", f.name, f.type, pos);
          switch (f.type) {
            case 1:
            case 12:
              System.out.printf("%d\n", sData[pos]);
              pos += 1;
              break;
            case 2:
              System.out.printf("%d\n", buffer.getShort(pos));
              pos += 2;
              break;
            case 3:
            case 8:
              System.out.printf("%d\n", buffer.getInt(pos));
              pos += 4;
              break;
            case 4:
            case 5:
            case 6:
            case 9:
              System.out.printf("%d\n", buffer.getLong(pos));
              pos += 8;
              break;
            case 7:
              int len = buffer.getInt(pos);
              pos += 4;
              pos += len;
              System.out.printf("%d\n", len);
              break;
            case 10:
              byte ver = sData[pos];
              pos += 1;
              if (ver == 4) {
                pos += 4;
              } else if (ver == 6) {
                pos += 16;
              }
              System.out.printf("%d\n", ver);
              break;
            case 11:
              int strLen = buffer.getInt(pos);
              pos += 4;
              byte[] strBytes = new byte[strLen];
              System.arraycopy(sData, pos, strBytes, 0, strLen);
              pos += strLen;
              System.out.printf("%s\n", new String(strBytes));
              break;
            default:
              throw new IllegalArgumentException("Unknown field type: " + f.type);
          }
        }
      }

      System.out.printf("%d %d %d\n", teimRecord, pos, sDataLen);

    } catch (IOException e) {
      e.printStackTrace();
    }

  }
}
