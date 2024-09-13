package com.example.common.myrequest;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class RequestDemoController1 {

    // @Autowired
    //private RestTemplate restTemplate;



    @GetMapping("/getParams")
    public String getParams(String a,int b) {
        return "get success";
    }


    @PostMapping("/postTest")
    public String postTest(HttpServletRequest request) {
        String age1 = request.getParameter("age");
        String name1 = request.getParameter("name");
        System.out.println("age1=" + age1 + ",name1=" + name1);

        new Thread(new Runnable() {
            @Override
            public void run() {
                String age2 = request.getParameter("age");
                String name2 = request.getParameter("name");
                System.out.println("age2=" + age2 + ",name2=" + name2);
                //模拟业务请求
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                age2 = request.getParameter("age");
                name2 = request.getParameter("name");
                System.out.println("age2=" + age2 + ",name2=" + name2);
            }
        }).start();
        return "post success";
    }

}
