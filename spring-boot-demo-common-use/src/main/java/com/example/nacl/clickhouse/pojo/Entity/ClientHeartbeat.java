package com.example.nacl.clickhouse.pojo.Entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("basic_client_heartbeat_local")
public class ClientHeartbeat {
    private String hostIp;
    private String hostName;
    private String systemUuid;
    private String hostId;
    private String valueType;
    private String type;
    private String itemid;
    private String units;
    private String key_;
    private String name;
    private String status;
    private String lastclock;
    private String lastns;
    private String lastvalue;
    private LocalDateTime insertTime;
} 