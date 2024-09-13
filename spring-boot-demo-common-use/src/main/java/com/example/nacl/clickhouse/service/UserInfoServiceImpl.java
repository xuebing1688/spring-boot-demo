package com.example.nacl.clickhouse.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.example.nacl.clickhouse.mapper.UserInfoMapper;
import com.example.nacl.clickhouse.pojo.Entity.UserInfo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @author Mr.NaCl
 * @since 2024/5/16
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {

    @Resource
    UserInfoMapper userInfoMapper;

    @Override
    public void saveData() {
        //
        String dateTimeText = "2000-05-08 19:00:00";
        DateTimeFormatter  formatter= DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Timestamp timestamp = Timestamp.valueOf(dateTimeText);
        String format = formatter.format(LocalDateTime.now());
        // 设置birthday

        // 当前时间
        ZonedDateTime  zonedDateTime = ZonedDateTime.now(ZoneId.of("Asia/Shanghai"));


        UserInfo userInfo = UserInfo.builder()
                .id(1)
                .name("小李")
                .joinDate(format)
                .birthday(format)
                .build();
        userInfoMapper.insert(userInfo);
    }

    @Override
    public UserInfo selectById(Integer id) {
        return userInfoMapper.selectById(id);
    }

    @Override
    public List<UserInfo> selectList() {
        List<UserInfo> userInfos = userInfoMapper.selectList(new LambdaQueryWrapper<>());
        return userInfos;
    }
}
