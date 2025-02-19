package com.example.config;

/**
 * @ClassName: $
 * @Description:
 * @Author: summer
 * @Date: 2024-12-12 10:49
 * @Version: 1.0
 **/
import javax.servlet.http.HttpServletRequest;
import com.alibaba.ttl.TransmittableThreadLocal;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class DefinitionRequestContext {

  public static TransmittableThreadLocal<HttpServletRequest> requestTransmittableThreadLocal = new TransmittableThreadLocal<>();

  public static void shareRequest(HttpServletRequest request) {
    requestTransmittableThreadLocal.set(request);
  }

  public static HttpServletRequest getRequest() {
    HttpServletRequest request = requestTransmittableThreadLocal.get();
    if (request!= null) {
      return requestTransmittableThreadLocal.get();
    } else {
      ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
      if (requestAttributes!= null) {
        return requestAttributes.getRequest();
      } else {
        return null;
      }
    }
  }

  public static void remove() {
    requestTransmittableThreadLocal.remove();
  }

}

