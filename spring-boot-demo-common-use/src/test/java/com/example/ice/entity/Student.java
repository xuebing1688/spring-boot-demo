package com.example.ice.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName: $
 * @Description:
 * @Author: summer
 * @Date: 2024-12-06 18:26
 * @Version: 1.0
 **/
@Data
@Getter
@Setter
public class Student extends User {

  private String school;
  private Integer grade;
  private Integer classNum;
  private Integer studentNum;
  private String  string;


}
