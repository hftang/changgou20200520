package com.changgou.framework.intercepter;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/**
 * @Auther: hftang
 * @Date: 2020/3/3 16:15
 * @Description: 全局的拦截器，在哪个微服务中需要 就在哪个微服务中创建
 */
public class FeignInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {

        try {
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (requestAttributes != null) {
                HttpServletRequest request = requestAttributes.getRequest();
                Enumeration<String> headerNames = request.getHeaderNames();

                if (headerNames != null) {
                    while (headerNames.hasMoreElements()) {
                        String headerName = headerNames.nextElement();
                        String headerValue = request.getHeader(headerName);
                        System.out.println("name:" + headerName + "::::::::value:" + headerValue);
                        requestTemplate.header(headerName, headerValue);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }



    }
}
