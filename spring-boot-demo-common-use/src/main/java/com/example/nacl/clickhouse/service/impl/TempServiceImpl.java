package com.example.nacl.clickhouse.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.example.nacl.clickhouse.mapper.TempMapper;
import com.example.nacl.clickhouse.mapper.WikiStatMapper;
import com.example.nacl.clickhouse.pojo.Entity.Temp;
import com.example.nacl.clickhouse.pojo.Entity.WikiStat;
import com.example.nacl.clickhouse.service.TempService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.sql.SQLException;

@Service
public class TempServiceImpl extends ServiceImpl<TempMapper, Temp> implements TempService {
    private static Logger logger= LoggerFactory.getLogger(TempServiceImpl.class);
    //Logger logger = Logger.getLogger("TempServiceImpl");
    @Autowired
    TempMapper userInfoMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private WikiStatMapper wikiStatMapper;

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

    @Override
    public void insertWikiTestData() {
        String sql = "INSERT INTO wikistat (time, project, subproject, path, hits) VALUES (?, ?, ?, ?, ?)";
        
        // 使用上海时区
        ZoneId shanghaiZone = ZoneId.of("Asia/Shanghai");
        // 当前时间（上海时区）
        LocalDateTime now = LocalDateTime.now(shanghaiZone);
        
        // 准备测试数据
        List<WikiStat> wikiStats = new ArrayList<>();
        
        // 定义一些可选值
        String[] projects = {"wikipedia", "wiktionary", "wikibooks", "wikinews", "wikiquote"};
        String[] subprojects = {"en", "zh", "es", "fr", "de", "ru", "ja", "it", "pt", "ar"};
        String[] paths = {
            "/wiki/Main_Page",
            "/wiki/Technology",
            "/wiki/Science",
            "/wiki/Arts",
            "/wiki/History",
            "/wiki/Geography",
            "/wiki/Sports",
            "/wiki/Politics",
            "/wiki/Entertainment",
            "/wiki/Education"
        };
        
        // 生成10000条随机数据
        for (int i = 0; i < 10000; i++) {
            WikiStat stat = new WikiStat();
            
            // 随机时间：当前时间往前推0-30天的随机时间点
            int randomDays = (int) (Math.random() * 30);
            int randomHours = (int) (Math.random() * 24);
            int randomMinutes = (int) (Math.random() * 60);
            int randomSeconds = (int) (Math.random() * 60);
            LocalDateTime randomTime = now
                .minusDays(randomDays)
                .minusHours(randomHours)
                .minusMinutes(randomMinutes)
                .minusSeconds(randomSeconds);
            
            stat.setTime(randomTime);
            
            // 随机项目
            stat.setProject(projects[(int) (Math.random() * projects.length)]);
            
            // 随机子项目
            stat.setSubproject(subprojects[(int) (Math.random() * subprojects.length)]);
            
            // 随机路径
            stat.setPath(paths[(int) (Math.random() * paths.length)]);
            
            // 随机访问量：100-10000之间
            stat.setHits(100L + (long) (Math.random() * 9900));
            
            wikiStats.add(stat);
        }

        try {
            // 分批执行，每批1000条
            int batchSize = 1000;
            for (int i = 0; i < wikiStats.size(); i += batchSize) {
                int endIndex = Math.min(i + batchSize, wikiStats.size());
                List<WikiStat> batch = wikiStats.subList(i, endIndex);
                
                jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int j) throws SQLException {
                        WikiStat stat = batch.get(j);
                        ZonedDateTime zonedDateTime = stat.getTime().atZone(shanghaiZone);
                        ps.setTimestamp(1, Timestamp.from(zonedDateTime.toInstant()));
                        ps.setString(2, stat.getProject());
                        ps.setString(3, stat.getSubproject());
                        ps.setString(4, stat.getPath());
                        ps.setLong(5, stat.getHits());
                    }

                    @Override
                    public int getBatchSize() {
                        return batch.size();
                    }
                });
                
                logger.info("成功插入第{}批数据，进度: {}/{}", (i/batchSize + 1), endIndex, wikiStats.size());
            }
        } catch (Exception e) {
            logger.error("批量插入WikiStat数据失败", e);
            throw new RuntimeException("批量插入WikiStat数据失败", e);
        }
    }
}
