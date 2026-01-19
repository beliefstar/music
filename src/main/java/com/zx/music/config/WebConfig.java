package com.zx.music.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        Resource r = new FileSystemResource("store/");
//        registry.addResourceHandler("/music/**")
//                .addResourceLocations(r)
//                .setCacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic());
//    }
}
