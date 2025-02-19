package com.example.common.config;

import com.example.common.entity.KafkaRecord;

import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ClickHouseManager {
    private static final String JDBC_URL = "jdbc:clickhouse://localhost:8123/your_database";
    private static final String USER = "default";
    private static final String PASSWORD = "";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
    }

    public static void batchInsertRecords(List<KafkaRecord> records) throws SQLException {
        if (records.isEmpty()) {
            return;
        }

        // 获取第一条记录的字段来构建SQL
        KafkaRecord firstRecord = records.get(0);
        String fields = "netlink_time, netlink_id, " +
            String.join(", ", firstRecord.getFields().keySet());

        String placeholders = "?, ?, " +
            firstRecord.getFields().keySet().stream()
                .map(k -> "?")
                .collect(Collectors.joining(", "));

        String sql = String.format(
            "INSERT INTO kafka_records (%s) VALUES (%s)",
            fields, placeholders
        );

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (KafkaRecord record : records) {
                int paramIndex = 1;
                pstmt.setLong(paramIndex++, record.getNetlinkTime());
                pstmt.setInt(paramIndex++, record.getNetlinkId());

                for (Object value : record.getFields().values()) {
                    if (value instanceof String) {
                        pstmt.setString(paramIndex++, (String) value);
                    } else if (value instanceof Integer) {
                        pstmt.setInt(paramIndex++, (Integer) value);
                    } else if (value instanceof Long) {
                        pstmt.setLong(paramIndex++, (Long) value);
                    } else if (value instanceof Byte) {
                        pstmt.setByte(paramIndex++, (Byte) value);
                    } else {
                        pstmt.setObject(paramIndex++, value);
                    }
                }
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }
}
