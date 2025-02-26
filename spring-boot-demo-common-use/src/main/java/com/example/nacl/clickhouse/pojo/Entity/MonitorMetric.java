package com.example.nacl.clickhouse.pojo.Entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("tbl_monitor_metric_local")
public class MonitorMetric {
    private LocalDateTime timeStamp;
    private String hostId;
    private String hostName;
    private String hostIp;
    private Double cpuUsageRate;
    private Long memUsage;
    private Double memUsageRate;
    private Long diskUsage;
    private Double diskUsageRate;
    private Double diskIoUsageRate;
    private Double diskReadAndWriteSpeed;
    private Long hostIops;
    private Long status;
    private LocalDateTime insertTime;
}
