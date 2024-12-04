package com.example.common.controller;


import com.example.common.util.ESUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EsController {

  @Autowired
  private ESUtils esUtils;


  //requset
  @RequestMapping("createIndex")
  public String test() {
    esUtils.createIndex("person");
    return "test";
  }

}
