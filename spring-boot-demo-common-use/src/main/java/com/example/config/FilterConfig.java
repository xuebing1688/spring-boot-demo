package com.example.config;

import com.example.filter.DefinitionRequestContextFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName: $
 * @Description:
 * @Author: summer
 * @Date: 2024-12-20 11:37
 * @Version: 1.0
 **/
@Configuration
public class FilterConfig {
  @Bean
  public FilterRegistrationBean<DefinitionRequestContextFilter> loggingFilter() {
    FilterRegistrationBean<DefinitionRequestContextFilter> registrationBean = new FilterRegistrationBean<>();
    registrationBean.setFilter(new DefinitionRequestContextFilter());
    registrationBean.addUrlPatterns("/*");  // 适用的URL模式
    return registrationBean;
  }
}
