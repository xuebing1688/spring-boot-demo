package com.example.spring.controller;


import com.example.config.DefinitionRequestContext;
import com.example.filter.DefinitionRequestContextFilter;
import com.example.spring.service.RetryableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class RetryableController {

    @Autowired
    private RetryableService retryableService;
  @Autowired
  private DefinitionRequestContextFilter definitionRequestContextFilter;

  @RequestMapping("retry")
    public String retry(Integer code){
        int test = 0;
        try {
            test = retryableService.test(code);
        } catch (Exception e) {
            e.printStackTrace();
        }
       return  String.valueOf(test);
    }

  @RequestMapping("setRequest")
  public void setRequest(HttpServletRequest request){
    DefinitionRequestContext.shareRequest(request);
    System.out.println("setRequest success");
  }

}
