package com.example.nacl.clickhouse.service;


import com.example.nacl.clickhouse.pojo.Entity.Temp;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TempService {

    // 写入数据
    void saveData (Temp userInfo) ;
    // ID 查询
    Temp selectById (@Param("id") String id) ;
    // 查询全部
    List<Temp> selectList () ;
}
