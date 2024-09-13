package com.example.nacl.clickhouse.controller;


import com.example.nacl.clickhouse.pojo.Entity.Temp;
import com.example.nacl.clickhouse.service.TempService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/temp")
public class TempController {
    @Autowired
    TempService tempService;

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



}
