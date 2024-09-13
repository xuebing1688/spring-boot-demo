package com.example.nacl.clickhouse.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.example.nacl.clickhouse.mapper.TempMapper;
import com.example.nacl.clickhouse.pojo.Entity.Temp;
import com.example.nacl.clickhouse.service.TempService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TempServiceImpl extends ServiceImpl<TempMapper, Temp> implements TempService {
    private static Logger logger= LoggerFactory.getLogger(TempServiceImpl.class);
    //Logger logger = Logger.getLogger("TempServiceImpl");
    @Autowired
    TempMapper userInfoMapper;

    @Override
    public void saveData(Temp userInfo) {
        userInfoMapper.saveData(userInfo);
    }

    @Override
    public Temp selectById(String id) {
        logger.info("日志："+id);
        return userInfoMapper.selectById(id);
    }

    @Override
    public List<Temp> selectList() {
        return userInfoMapper.selectList();
    }
}
