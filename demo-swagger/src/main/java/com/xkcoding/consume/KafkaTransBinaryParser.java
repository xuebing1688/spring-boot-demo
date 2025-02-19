package com.xkcoding.consume;

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
    private byte[] recordData = new byte[1024 * 1024 * 800]; // 800MB buffer as in C++ code
    private int recordDataLen = 0;

    public void parseFile(String filePath) throws IOException {
        byte[] fileContent = Files.readAllBytes(Paths.get(filePath));
        System.out.printf("Total file size: %d bytes%n", fileContent.length);

        if (fileContent.length < 4) {
            throw new IOException("File is too small");
        }

        // Get total size from header (little endian)
        int totalSize = getInt(fileContent, 0);
        System.out.printf("Total size from header: %d bytes%n", totalSize);

        // Skip size header
        data = new byte[fileContent.length - 4];
        System.arraycopy(fileContent, 4, data, 0, fileContent.length - 4);
        dataLength = fileContent.length - 4;

        parseData();
    }

    private void parseData() {
        position = 0;

        while (position + 10 <= dataLength) {
            System.out.printf("\nProcessing at position: %d%n", position);

            KafkaTransBinaryHead header = parseHeader();
            if (header == null) break;

            printHeader(header);
            processDataChunk(header);

            // Skip to next chunk
            if (position < dataLength) position++;
        }

        if (recordDataLen > 0) {
            System.out.printf("\nProcessing collected data (%d bytes)%n", recordDataLen);
            parseRecordData();
        }
    }

    private void parseRecordData() {
        position = 0;
        data = recordData;
        dataLength = recordDataLen;

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
            if (position + 10 > dataLength) { // Ensure there's enough data for the header
                System.out.println("Not enough data to parse header");
                return null;
            }

            KafkaTransBinaryHead head = new KafkaTransBinaryHead();

            // magic
            head.setMagic(data[position++]);

            // headerLen
            head.setHeaderLen(data[position++]);

            // Combined byte for ipVersion and flags
            byte flags = data[position++];
            head.setIpVersion((byte) ((flags >> 4) & 0x0F));  // Upper 4 bits
            head.setReserve((byte) ((flags >> 2) & 0x03));    // Next 2 bits
            head.setFirst(((flags >> 1) & 0x01) != 0);        // Next 1 bit
            head.setDone((flags & 0x01) != 0);                // Last 1 bit

            // netlinkId
            head.setNetlinkId(data[position++]);

            // dataLen (little endian)
            head.setDataLen(getInt(data, position));
            position += 4;

            // sequence (little endian)
            head.setSequence(getShort(data, position));
            position += 2;

            // pktCount (little endian)
            head.setPktCount(getShort(data, position));
            position += 2;

            return head;
        } catch (Exception e) {
            System.out.println("Error parsing header: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private List<Field> parseFields() {
        try {
            List<Field> fields = new ArrayList<>();

            // Print first 32 bytes for debugging
            System.out.println("\nFirst 32 bytes of record data:");
            for (int i = 0; i < Math.min(32, dataLength); i++) {
                System.out.printf("%02X ", data[i] & 0xFF);
                if ((i + 1) % 16 == 0) System.out.println();
            }
            System.out.println();

            if (position + 2 > dataLength) {
                System.out.println("Not enough data to read field count");
                return null;
            }

            // Read field count (little-endian)
            int fieldCount = data[position] & 0xFF;  // 只读取一个字节作为字段数
            position++;

            System.out.printf("Field count: %d at position %d%n", fieldCount, position);

            if (fieldCount <= 0 || fieldCount > 100) {
                System.out.printf("Invalid field count: %d%n", fieldCount);
                return null;
            }

            // Skip one byte
            position++;

            for (int i = 0; i < fieldCount; i++) {
                // Read field name until null terminator or max length
                StringBuilder fieldName = new StringBuilder();
                int nameLen = 0;
                while (position < dataLength && nameLen < 255) {
                    byte b = data[position++];
                    if (b == 0) break;  // null terminator
                    fieldName.append((char)b);
                    nameLen++;
                }

                if (nameLen == 0 || nameLen >= 255) {
                    System.out.printf("Invalid field name length: %d at position %d%n", nameLen, position);
                    return null;
                }

                // Read field type
                if (position >= dataLength) {
                    System.out.println("Not enough data to read field type");
                    return null;
                }
                byte fieldType = data[position++];

                System.out.printf("Field %d: name='%s', type=%d%n",
                    i, fieldName.toString(), fieldType);
                fields.add(new Field(fieldType, fieldName.toString()));
            }

            return fields;

        } catch (Exception e) {
            System.out.println("Error parsing fields: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private void parseNetlinkInfo(List<Field> fields) {
        // 按照C++代码的解析顺序
        byte netlinkCount = data[position++];
        short netlikId = ByteOrderUtils.ntohs(getShort(data, position));
        position += 2;

        int netlikRecord = ByteOrderUtils.ntohl(getInt(data, position));
        position += 4;

        long netlinkTime = ByteOrderUtils.ntohll(getLong(data, position));
        position += 8;

        int timeRecord = ByteOrderUtils.ntohl(getInt(data, position));
        position += 4;

        System.out.printf("%d %d %d %d %d%n",
            netlinkCount, netlikId, netlikRecord, netlinkTime, timeRecord);

        parseRecords(fields, timeRecord);
    }

    private void parseRecords(List<Field> fields, int timeRecord) {
        while (timeRecord > 0 && position < dataLength) {
            timeRecord--;
            for (Field field : fields) {
                System.out.printf("%s %d %d ", field.name, field.type, position);

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
        System.out.printf("headlen: %d, ipVersion: %d, reserve: %d, isFirst: %d, isDone: %d, " +
                         "netlinkId: %d, dataLen: %d, seq: %d, pktCount: %d, taskId: %d%n",
            head.getHeaderLen(),
            head.getIpVersion(),
            head.getReserve(),
            head.isFirst() ? 1 : 0,
            head.isDone() ? 1 : 0,
            head.getNetlinkId() & 0xFF,  // Convert to unsigned
            head.getDataLen(),
            head.getSequence() & 0xFFFF,  // Convert to unsigned
            head.getPktCount() & 0xFFFF,  // Convert to unsigned
            getShort(data, position + 4) & 0xFFFF); // taskId is 2 bytes after ipaddr
    }

    private short getShort(byte[] data, int offset) {
        return (short) ((data[offset] & 0xFF) |
                       ((data[offset + 1] & 0xFF) << 8));
    }

    private int getInt(byte[] data, int offset) {
        return (data[offset] & 0xFF) |
               ((data[offset + 1] & 0xFF) << 8) |
               ((data[offset + 2] & 0xFF) << 16) |
               ((data[offset + 3] & 0xFF) << 24);
    }

    private long getLong(byte[] data, int offset) {
        return (data[offset] & 0xFF) |
               ((long)(data[offset + 1] & 0xFF) << 8) |
               ((long)(data[offset + 2] & 0xFF) << 16) |
               ((long)(data[offset + 3] & 0xFF) << 24) |
               ((long)(data[offset + 4] & 0xFF) << 32) |
               ((long)(data[offset + 5] & 0xFF) << 40) |
               ((long)(data[offset + 6] & 0xFF) << 48) |
               ((long)(data[offset + 7] & 0xFF) << 56);
    }

    private void processDataChunk(KafkaTransBinaryHead header) {
        try {
            int dataLen = header.getDataLen();
            System.out.printf("proc %d %d%n", dataLen, recordDataLen);

            // Skip IP address
            int ipAddrLen = (header.getIpVersion() == 4) ? 4 : 16;
            if (position + ipAddrLen + 2 > dataLength) {
                System.out.println("Not enough data for IP address and taskId");
                return;
            }

            // Skip IP address and read taskId
            position += ipAddrLen;
            short taskId = getShort(data, position);
            position += 2;

            // Calculate actual data length
            int actualDataLen = dataLen - 2;  // Only subtract taskId length
            if (actualDataLen <= 0) {
                System.out.println("Invalid actual data length");
                return;
            }

            // Copy data
            if (position + actualDataLen <= dataLength) {
                System.arraycopy(data, position, recordData, recordDataLen, actualDataLen);
                recordDataLen += actualDataLen;
                position += actualDataLen;
                System.out.printf("Copied %d bytes, total: %d%n", actualDataLen, recordDataLen);
            } else {
                System.out.printf("Data length exceeds buffer: need %d, have %d%n",
                    actualDataLen, dataLength - position);
            }
        } catch (Exception e) {
            System.out.printf("Error processing chunk: %s%n", e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            KafkaTransBinaryParser parser = new KafkaTransBinaryParser();
            parser.parseFile(Constant.KAFKA_LOG_Data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
