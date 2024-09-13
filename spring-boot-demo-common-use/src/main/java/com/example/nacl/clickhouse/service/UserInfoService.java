package com.example.nacl.clickhouse.service;

import com.baomidou.mybatisplus.extension.service.IService;

import com.example.nacl.clickhouse.pojo.Entity.UserInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Mr.NaCl
 * @since 2024/5/16
 */
public interface UserInfoService extends IService<UserInfo> {

    // 写入数据
    void saveData () ;
    // ID 查询
    UserInfo selectById (@Param("id") Integer id) ;
    // 查询全部
    List<UserInfo> selectList () ;
}
