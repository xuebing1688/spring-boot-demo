package com.example.nacl.clickhouse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.nacl.clickhouse.pojo.Entity.UserInfo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @author Mr.NaCl
 * @since 2024/5/16
 */
@Mapper
public interface UserInfoMapper extends BaseMapper<UserInfo> {

}
