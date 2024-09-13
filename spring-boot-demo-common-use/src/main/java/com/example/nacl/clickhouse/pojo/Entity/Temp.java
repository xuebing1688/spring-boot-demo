package com.example.nacl.clickhouse.pojo.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class Temp implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private BigDecimal amount;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date dateColulmn;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date DateTimeClumn;
    private Integer int16Column;
    private Long uint64Column;
}
