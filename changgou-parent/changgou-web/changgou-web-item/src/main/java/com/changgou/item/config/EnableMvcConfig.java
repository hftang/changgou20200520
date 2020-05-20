package com.changgou.item.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Auther: hftang
 * @Date: 2020/2/21 10:49
 * @Description: 添加允许访问的页面
 */

@ControllerAdvice
@Configuration
public class EnableMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        registry.addResourceHandler("/items/**")
                .addResourceLocations("classpath:/templates/items/");
    }
}
