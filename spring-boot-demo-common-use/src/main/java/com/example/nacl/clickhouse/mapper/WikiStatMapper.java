package com.example.nacl.clickhouse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.nacl.clickhouse.pojo.Entity.WikiStat;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WikiStatMapper extends BaseMapper<WikiStat> {
    void batchInsert(@Param("list") List<WikiStat> list);
} 