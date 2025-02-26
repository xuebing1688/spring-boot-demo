package com.example.nacl.clickhouse.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.nacl.clickhouse.pojo.Entity.ClientHeartbeat;

public interface ClientHeartbeatService extends IService<ClientHeartbeat> {
    void insertHeartbeatTestData();
} 