package com.example.nacl.clickhouse.pojo.Entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("wikistat")
public class WikiStat {
    private LocalDateTime time;
    private String project;
    private String subproject;
    private String path;
    private Long hits;
} 