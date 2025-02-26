package com.example.nacl.clickhouse.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.nacl.clickhouse.mapper.MonitorMetricMapper;
import com.example.nacl.clickhouse.pojo.Entity.MonitorMetric;
import com.example.nacl.clickhouse.service.MonitorMetricService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class MonitorMetricServiceImpl extends ServiceImpl<MonitorMetricMapper, MonitorMetric> implements MonitorMetricService {

    @Autowired
    private MonitorMetricMapper monitorMetricMapper;

    @Override
    public void insertMonitorMetricData() {
        // 使用上海时区
        ZoneId shanghaiZone = ZoneId.of("Asia/Shanghai");
        // 获取当前时间的下一个小时
        LocalDateTime nextHour = LocalDateTime.now(shanghaiZone)
                .plusHours(1)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
        
        List<MonitorMetric> metrics = new ArrayList<>();
        
        // 模拟10个不同的主机
        String[] hostIds = new String[10];
        String[] hostNames = new String[10];
        String[] hostIps = new String[10];
        for (int i = 0; i < 10; i++) {
            hostIds[i] = "HOST_" + String.format("%03d", i);
            hostNames[i] = "host-" + String.format("%03d", i);
            hostIps[i] = "192.168.168." + (130 + i);
        }
        
        // 对每个主机生成下一个小时的5条数据
        for (int hostIndex = 0; hostIndex < 10; hostIndex++) {
            // 每小时生成5条数据
            for (int recordsPerHour = 0; recordsPerHour < 5; recordsPerHour++) {
                MonitorMetric metric = new MonitorMetric();
                
                // 在下一个小时内随机生成分钟和秒
                int randomMinutes = (int) (Math.random() * 60);
                int randomSeconds = (int) (Math.random() * 60);
                
                LocalDateTime recordTime = nextHour
                    .withMinute(randomMinutes)
                    .withSecond(randomSeconds);
                
                metric.setTimeStamp(recordTime);
                metric.setHostId(hostIds[hostIndex]);
                metric.setHostName(hostNames[hostIndex]);
                metric.setHostIp(hostIps[hostIndex]);
                
                // 生成随机监控数据
                metric.setCpuUsageRate(Math.random() * 100);
                metric.setMemUsage((long) (Math.random() * 32 * 1024 * 1024 * 1024));
                metric.setMemUsageRate(Math.random() * 100);
                metric.setDiskUsage((long) (Math.random() * 1024 * 1024 * 1024 * 1024));
                metric.setDiskUsageRate(Math.random() * 100);
                metric.setDiskIoUsageRate(Math.random() * 100);
                metric.setDiskReadAndWriteSpeed(Math.random() * 500);
                metric.setHostIops((long) (Math.random() * 10000));
                metric.setStatus(Math.random() > 0.9 ? 0L : 1L);
                metric.setInsertTime(recordTime);
                
                metrics.add(metric);
            }
        }

        try {
            // 分批执行，每批100条
            int batchSize = 100;
            for (int i = 0; i < metrics.size(); i += batchSize) {
                int endIndex = Math.min(i + batchSize, metrics.size());
                List<MonitorMetric> batch = metrics.subList(i, endIndex);
                
                // 使用循环单条插入
                for (MonitorMetric metric : batch) {
                    monitorMetricMapper.insert(metric);
                }
                
                log.info("成功插入第{}批监控数据，进度: {}/{}", 
                    (i/batchSize + 1), endIndex, metrics.size());
            }
        } catch (Exception e) {
            log.error("批量插入监控数据失败", e);
            throw new RuntimeException("批量插入监控数据失败", e);
        }
    }
} 