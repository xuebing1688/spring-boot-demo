package com.example.nacl.clickhouse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.nacl.clickhouse.pojo.Entity.ClientHeartbeat;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ClientHeartbeatMapper extends BaseMapper<ClientHeartbeat> {
} 