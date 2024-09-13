package com.example.nacl.clickhouse.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.nacl.clickhouse.pojo.Entity.Temp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TempMapper extends BaseMapper<Temp> {

    void saveData (Temp temp) ;
    // ID 查询
    Temp selectById (@Param("id") String id) ;
    // 查询全部
    List<Temp> selectList () ;
}
