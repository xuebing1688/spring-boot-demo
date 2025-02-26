package com.example.nacl.clickhouse.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.nacl.clickhouse.pojo.Entity.MonitorMetric;

public interface MonitorMetricService extends IService<MonitorMetric> {
    void insertMonitorMetricData();
} 