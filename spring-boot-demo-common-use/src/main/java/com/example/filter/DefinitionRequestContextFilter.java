package com.example.filter;

/**
 * @ClassName: $
 * @Description:
 * @Author: summer
 * @Date: 2024-12-12 10:49
 * @Version: 1.0
 **/
import com.example.config.DefinitionRequestContext;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Component
public class DefinitionRequestContextFilter implements Filter {


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 初始化逻辑，例如加载配置等。
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
         //   DefinitionRequestContext.shareRequest((HttpServletRequest) request);
            chain.doFilter(request, response);
        } finally {

        }
    }

    @Override
    public void destroy() {

    }
        // 清理

}

