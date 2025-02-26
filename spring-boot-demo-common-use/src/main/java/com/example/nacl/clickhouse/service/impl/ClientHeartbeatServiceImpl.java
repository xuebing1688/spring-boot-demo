package com.example.nacl.clickhouse.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.nacl.clickhouse.mapper.ClientHeartbeatMapper;
import com.example.nacl.clickhouse.pojo.Entity.ClientHeartbeat;
import com.example.nacl.clickhouse.service.ClientHeartbeatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ClientHeartbeatServiceImpl extends ServiceImpl<ClientHeartbeatMapper, ClientHeartbeat> implements ClientHeartbeatService {

    @Autowired
    private ClientHeartbeatMapper clientHeartbeatMapper;

    @Override
    public void insertHeartbeatTestData() {
        // 使用上海时区
        ZoneId shanghaiZone = ZoneId.of("Asia/Shanghai");
        LocalDateTime now = LocalDateTime.now(shanghaiZone).withNano(0);
        
        // 准备测试数据
        List<ClientHeartbeat> heartbeats = new ArrayList<>();
        
        // 模拟10个不同的主机，IP从192.168.58.130开始
        String[] hostIps = new String[10];
        String[] hostNames = new String[10];
        String[] hostIds = new String[10];
        String[] systemUuids = new String[10];
        for (int i = 0; i < 10; i++) {
            // 从192.168.58.130开始递增IP
            hostIps[i] = "192.168.58." + (130 + i);
            hostNames[i] = "host-" + String.format("%03d", i);
            hostIds[i] = "HOST_" + String.format("%03d", i);
            systemUuids[i] = UUID.randomUUID().toString();
        }
        
        // 预定义一些可能的值
        String[] valueTypes = {"float", "integer", "string"};
        String[] types = {"agent", "snmp", "ipmi"};
        String[] units = {"B", "bps", "%", "ms"};
        String[] keys = {"system.cpu.load", "vm.memory.size", "net.if.in", "system.uptime"};
        String[] names = {"CPU Load", "Memory Usage", "Network Input", "System Uptime"};
        String[] statuses = {"0", "1"}; // 0: 异常, 1: 正常

        // 生成1000条随机数据
        for (int i = 0; i < 1000; i++) {
            ClientHeartbeat heartbeat = new ClientHeartbeat();
            
            // 随机选择一个主机
            int hostIndex = (int) (Math.random() * 10);
            
            heartbeat.setHostIp(hostIps[hostIndex]);
            heartbeat.setHostName(hostNames[hostIndex]);
            heartbeat.setSystemUuid(systemUuids[hostIndex]);
            heartbeat.setHostId(hostIds[hostIndex]);
            
            // 随机设置其他属性
            heartbeat.setValueType(valueTypes[(int) (Math.random() * valueTypes.length)]);
            heartbeat.setType(types[(int) (Math.random() * types.length)]);
            heartbeat.setItemid(String.format("%d", (long)(Math.random() * 1000000)));
            heartbeat.setUnits(units[(int) (Math.random() * units.length)]);
            heartbeat.setKey_(keys[(int) (Math.random() * keys.length)]);
            heartbeat.setName(names[(int) (Math.random() * names.length)]);
            heartbeat.setStatus(statuses[(int) (Math.random() * statuses.length)]);
            
            // 设置最后检测时间和值
            heartbeat.setLastclock(String.valueOf(System.currentTimeMillis() / 1000));
            heartbeat.setLastns("0");
            heartbeat.setLastvalue(String.format("%.2f", Math.random() * 100));
            
            // 设置插入时间
            heartbeat.setInsertTime(now);
            
            heartbeats.add(heartbeat);
        }

        try {
            // 分批执行，每批100条
            int batchSize = 100;
            for (int i = 0; i < heartbeats.size(); i += batchSize) {
                int endIndex = Math.min(i + batchSize, heartbeats.size());
                List<ClientHeartbeat> batch = heartbeats.subList(i, endIndex);
                
                // 使用循环单条插入
                for (ClientHeartbeat heartbeat : batch) {
                    clientHeartbeatMapper.insert(heartbeat);
                }
                
                log.info("成功插入第{}批心跳数据，进度: {}/{}", 
                    (i/batchSize + 1), endIndex, heartbeats.size());
            }
        } catch (Exception e) {
            log.error("批量插入心跳数据失败", e);
            throw new RuntimeException("批量插入心跳数据失败", e);
        }
    }
} 