package com.example.nacl.clickhouse.controller;


import com.example.nacl.clickhouse.pojo.Entity.Temp;
import com.example.nacl.clickhouse.service.TempService;
import com.example.nacl.clickhouse.service.MonitorMetricService;
import com.example.nacl.clickhouse.service.ClientHeartbeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

@RestController
@RequestMapping("/temp")
public class TempController {
    @Autowired
    TempService tempService;

    @Autowired
    private MonitorMetricService monitorMetricService;

    @Autowired
    private ClientHeartbeatService clientHeartbeatService;

    @GetMapping("/selectById/{id}")
    public Temp selectById(@PathVariable("id") String id){
        return tempService.selectById(id);
    }

    @PostMapping("/saveData")
    public void saveData(@RequestBody Temp userInfo){
        tempService.saveData(userInfo);
    }

    @GetMapping("/selectList")
    public List<Temp> selectList(){
        return tempService.selectList();
    }

    @GetMapping("/insertWikiTestData")
    public void insertWikiTestData() {
        tempService.insertWikiTestData();
    }

    @GetMapping("/insertHeartbeatTestData")
    public void insertHeartbeatTestData() {
        clientHeartbeatService.insertHeartbeatTestData();
    }

    // 添加定时任务，每小时整点执行
    @Scheduled(cron = "0 0 * * * ?")
    public void scheduledInsertMonitorMetricData() {
        monitorMetricService.insertMonitorMetricData();
    }


    @GetMapping("/insertMonitorMetricData")
    public void insertMonitorMetricData() {
    monitorMetricService.insertMonitorMetricData();
  }

}
