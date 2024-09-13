package com.example.common.exception;

import com.example.common.callmsg.CallResultMsg;
import com.example.common.callmsg.UniformReponseHandler;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * @Author:
 * @Date: 2020/7/30 18:26
 * @Description:
 */

@Controller
public class MyNotFoundException implements ErrorController {

    @Override
    public String getErrorPath() {
        return "/error";
    }

    @RequestMapping(value = "/error")
    @ResponseBody
    public CallResultMsg getError() {
        return new UniformReponseHandler<Map>().notFoundErrorResponse_System();
    }

}
