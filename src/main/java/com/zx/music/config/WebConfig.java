package com.zx.music.config;

import cn.hutool.core.util.ReUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        Resource r = new FileSystemResource("store/");
//        registry.addResourceHandler("/music/**")
//                .addResourceLocations(r)
//                .setCacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic());
//    }


    public static void main(String[] args) throws InterruptedException {
//        String s = "<a href=\"thread-390091.htm\">杨丞琳《<em>不为</em><em>谁而</em><em>作<em>的歌</em></em>(live)》歌手2024第2期专辑全套[MP3-320K]原版未修音</a>";
        String s = "<a href=\"thread-390091.htm\">杨丞琳《<em>不为</em><em>谁而</em><em>作<em>的歌</em></em>(live)》[MP3-320K]</a>";
//        String s = "<a href=\"thread-413906.htm\">【卡地亚TRINITY 100】林俊杰《愿与愁》+《Like You Do》+《<em>不为</em><em>谁而</em><em>作<em>的歌</em></em>》Live WAV</a>";
        parseSearchItem(s);

    }

    public static void parseSearchItem(String it) {
        String ref = extractRef(it);
        String name = extractTitle(it);
        System.out.println("name:" + name + " ref:" + ref);
    }

    private static String extractRef(String s) {
        List<String> titles = ReUtil.findAll("<a href=\"(thread-\\w+.htm)\">.*?</a>", s, 1);
        for (String title : titles) {
            title = title.replace("<em>", "");
            title = title.replace("</em>", "");
            title = title.trim();
            return title;
        }
        return "";
    }
    private static String extractTitle(String s) {
        List<String> titles = ReUtil.findAll("<a href=\"thread-\\w+.htm\">(.*?)(\\[.*?])?</a>", s, 1);
        for (String title : titles) {
            title = title.replace("<em>", "");
            title = title.replace("</em>", "");
            title = title.trim();
            return title;
        }
        return "";
    }
}
