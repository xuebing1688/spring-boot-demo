package com.example.nacl.clickhouse.controller;

import com.example.nacl.clickhouse.pojo.Entity.UserInfo;
import com.example.nacl.clickhouse.service.UserInfoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Mr.NaCl
 * @since 2024/5/16
 */
@RestController
public class UserInfoController {
    @Resource
    private UserInfoService userInfoService;

    @GetMapping("/selectById/{id}")
    public UserInfo selectById(@PathVariable("id") Integer id){
        return userInfoService.getById(id);
    }

    @PostMapping("/saveData")
    public void saveData(){
        userInfoService.saveData();
    }

    @GetMapping("/selectList")
    public List<UserInfo> selectList(){
        return userInfoService.selectList();
    }
}
