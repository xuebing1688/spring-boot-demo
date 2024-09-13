package com.example.nacl.clickhouse.pojo.Entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Mr.NaCl
 * @since 2024/5/16
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName(value =  "user_info")
public class UserInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String name;
    private String     joinDate;
    private String   birthday;
    private String startTime;
    private String endTime;
}
