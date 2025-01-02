package com.xkcoding.kafkatrans;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.xkcoding.swagger.entity.Constant;

public class KafkaTransBinaryParser {
    private static class Field {
        int type;
        String name;

        public Field(int type, String name) {
            this.type = type;
            this.name = name;
        }
    }

    private byte[] data;
    private int position = 0;
    private int dataLength = 0;

    public void parseFile(String filePath) throws IOException {
        byte[] fileContent = Files.readAllBytes(Paths.get(filePath));
        if (fileContent.length < 4) {
            throw new IOException("File is too small - must be at least 4 bytes");
        }
        
        // Print the first 4 bytes for debugging
        System.out.printf("First 4 bytes: %02X %02X %02X %02X%n", 
            fileContent[0] & 0xFF, 
            fileContent[1] & 0xFF, 
            fileContent[2] & 0xFF, 
            fileContent[3] & 0xFF);
        
        // Get file size (first 4 bytes) - using little endian
        int fileSize = getInt(fileContent, 0);
        System.out.printf("File size from header: %d bytes%n", fileSize);
        
        if (fileSize <= 0 || fileSize > fileContent.length - 4) {
            // Try reading the actual content directly
            data = new byte[fileContent.length];
            System.arraycopy(fileContent, 0, data, 0, fileContent.length);
            dataLength = fileContent.length;
        } else {
            // Use the size from header
            data = new byte[fileSize];
            System.arraycopy(fileContent, 4, data, 0, fileSize);
            dataLength = fileSize;
        }
        
        System.out.printf("Using data length: %d bytes%n", dataLength);
        parseData();
    }

    private void parseData() {
        position = 0;
        
        // Parse header
        KafkaTransBinaryHead header = parseHeader();
        if (header == null) {
            System.out.println("Failed to parse header");
            return;
        }
        
        printHeader(header);
        
        // Validate header length
        if (header.getHeaderLen() <= 0 || header.getHeaderLen() > dataLength) {
            System.out.printf("Invalid header length: %d%n", header.getHeaderLen());
            return;
        }
        
        // Skip header and taskId
        position = header.getHeaderLen() + 4 + 2;
        if (position >= dataLength) {
            System.out.println("Data too short after header");
            return;
        }
        
        // Parse fields
        List<Field> fields = parseFields();
        if (fields == null || fields.isEmpty()) {
            System.out.println("No fields parsed");
            return;
        }
        
        // Parse netlink information
        parseNetlinkInfo(fields);
    }

    private KafkaTransBinaryHead parseHeader() {
        try {
            KafkaTransBinaryHead head = new KafkaTransBinaryHead();
            head.setMagic(data[position++]);
            head.setHeaderLen(data[position++]);
            
            byte flags = data[position++];
            head.setIpVersion((byte) (flags & 0x0F));
            head.setReserve((byte) ((flags >> 4) & 0x03));
            head.setFirst((flags & 0x40) != 0);
            head.setDone((flags & 0x80) != 0);
            
            head.setNetlinkId(data[position++]);
            head.setDataLen(ByteOrderUtils.ntohl(getInt(data, position)));
            position += 4;
            head.setSequence(ByteOrderUtils.ntohs(getShort(data, position)));
            position += 2;
            head.setPktCount(ByteOrderUtils.ntohs(getShort(data, position)));
            position += 2;
            
            return head;
        } catch (Exception e) {
            System.out.println("Error parsing header: " + e.getMessage());
            return null;
        }
    }

    private List<Field> parseFields() {
        try {
            List<Field> fields = new ArrayList<>();
            
            if (position + 2 > dataLength) {
                System.out.println("Not enough data to read field count");
                return null;
            }
            
            // Debug output
            System.out.printf("Reading field count at position %d: %02X %02X%n", 
                position, data[position] & 0xFF, data[position + 1] & 0xFF);
            
            // 读取字段数量
            short fieldCount = (short)(data[position + 1] & 0xFF);
            position += 2;
            
            System.out.println("Field count: " + fieldCount);
            
            if (fieldCount <= 0 || fieldCount > 100) {
                System.out.println("Invalid field count: " + fieldCount);
                return null;
            }
            
            for (int i = 0; i < fieldCount && position + 4 <= dataLength; i++) {
                // Debug output
                System.out.printf("Reading field length at position %d: %02X %02X %02X %02X%n",
                    position,
                    data[position] & 0xFF,
                    data[position + 1] & 0xFF,
                    data[position + 2] & 0xFF,
                    data[position + 3] & 0xFF);
                
                // 读取字段名长度
                int nameLen = data[position + 3] & 0xFF;
                position += 4;
                
                System.out.printf("Name length: %d at position %d%n", nameLen, position);
                
                if (nameLen <= 0 || nameLen > 254 || position + nameLen > dataLength) {
                    System.out.printf("Invalid field name length: %d at position %d%n", nameLen, position);
                    return null;
                }
                
                // 读取字段名
                String fieldName = new String(data, position, nameLen, "UTF-8");
                position += nameLen;
                
                if (position >= dataLength) {
                    System.out.println("Not enough data to read field type");
                    return null;
                }
                
                // 读取字段类型
                int fieldType = data[position++] & 0xFF;
                
                // 放宽字段类型的验证
                if (fieldType <= 0 || fieldType > 20) { // 扩大类型范围到20
                    System.out.printf("Warning: Unusual field type: %d for field %s%n", fieldType, fieldName);
                    // 继续处理，而不是返回null
                }
                
                fields.add(new Field(fieldType, fieldName));
                System.out.printf("Successfully parsed field: %s (len=%d) type=%d pos=%d%n", 
                    fieldName, nameLen, fieldType, position);
            }
            
            if (fields.isEmpty()) {
                System.out.println("No fields were parsed successfully");
                return null;
            }
            
            return fields;
            
        } catch (Exception e) {
            System.out.println("Error parsing fields: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private void parseNetlinkInfo(List<Field> fields) {
        byte netlinkCount = data[position++];
        short netlinkId = ByteOrderUtils.ntohs(getShort(data, position));
        position += 2;
        
        int netlinkRecord = ByteOrderUtils.ntohl(getInt(data, position));
        position += 4;
        
        long netlinkTime = ByteOrderUtils.ntohll(getLong(data, position));
        position += 8;
        
        int timeRecord = ByteOrderUtils.ntohl(getInt(data, position));
        position += 4;
        
        System.out.printf("Netlink info: count=%d id=%d record=%d time=%d timeRecord=%d%n",
                         netlinkCount, netlinkId, netlinkRecord, netlinkTime, timeRecord);
        
        parseRecords(fields, timeRecord);
    }

    private void parseRecords(List<Field> fields, int recordCount) {
        for (int i = 0; i < recordCount && position < dataLength; i++) {
            for (Field field : fields) {
                System.out.printf("%s type=%d pos=%d ", field.name, field.type, position);
                
                switch (field.type) {
                    case 1:
                    case 12:
                        System.out.println(data[position++] & 0xFF);
                        break;
                        
                    case 2:
                        System.out.println(ByteOrderUtils.ntohs(getShort(data, position)));
                        position += 2;
                        break;
                        
                    case 3:
                    case 8:
                        System.out.println(ByteOrderUtils.ntohl(getInt(data, position)));
                        position += 4;
                        break;
                        
                    case 4:
                    case 5:
                    case 6:
                    case 9:
                        System.out.println(ByteOrderUtils.ntohll(getLong(data, position)));
                        position += 8;
                        break;
                        
                    case 7:
                        int len = ByteOrderUtils.ntohl(getInt(data, position));
                        position += 4 + len;
                        System.out.println(len);
                        break;
                        
                    case 10:
                        byte ver = data[position++];
                        position += (ver == 4) ? 4 : (ver == 6) ? 16 : 0;
                        System.out.println(ver);
                        break;
                        
                    case 11:
                        int length = getInt(data, position);
                        position += 4 + length;
                        System.out.printf("%d %d%n", length, data[position - length]);
                        break;
                }
            }
        }
    }

    private void printHeader(KafkaTransBinaryHead head) {
        System.out.printf("Header: len=%d ipVersion=%d reserve=%d isFirst=%b isDone=%b " +
                         "netlinkId=%d dataLen=%d seq=%d pktCount=%d%n",
                         head.getHeaderLen(), head.getIpVersion(), head.getReserve(),
                         head.isFirst(), head.isDone(), head.getNetlinkId(),
                         head.getDataLen(), head.getSequence(), head.getPktCount());
    }

    private short getShort(byte[] data, int offset) {
        return (short) ((data[offset] & 0xFF) << 8 | (data[offset + 1] & 0xFF));
    }

    private int getInt(byte[] data, int offset) {
        return ((data[offset] & 0xFF) << 24) |
               ((data[offset + 1] & 0xFF) << 16) |
               ((data[offset + 2] & 0xFF) << 8) |
               (data[offset + 3] & 0xFF);
    }

    private long getLong(byte[] data, int offset) {
        return ((long) (data[offset] & 0xFF) << 56) |
               ((long) (data[offset + 1] & 0xFF) << 48) |
               ((long) (data[offset + 2] & 0xFF) << 40) |
               ((long) (data[offset + 3] & 0xFF) << 32) |
               ((long) (data[offset + 4] & 0xFF) << 24) |
               ((long) (data[offset + 5] & 0xFF) << 16) |
               ((long) (data[offset + 6] & 0xFF) << 8) |
               (data[offset + 7] & 0xFF);
    }

    public static void main(String[] args) {
        try {
            KafkaTransBinaryParser parser = new KafkaTransBinaryParser();
            parser.parseFile(Constant.KAFKA_LOG_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
} 