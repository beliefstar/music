package com.zx.music.lyric.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpGlobalConfig;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.zx.music.lyric.LrcValidator;
import com.zx.music.lyric.LyricProvider;
import com.zx.music.util.Comm;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.net.HttpCookie;
import java.util.List;

/**
 * @author xzhen
 * @creatAt 2026-02-25 17:17
 */
@Service
@Order(2)
public class XiaoJiangClubProvider implements LyricProvider {

    public static final String HOST = "https://xiaojiangclub.com";

    @Override
    public String getLyric(String musicName, String musicArtist) {
        String searchLink = search(musicName, musicArtist);
        if (StrUtil.isBlank(searchLink)) {
            return null;
        }
        String lyricContent = getByLyricLink(searchLink);
        if (LrcValidator.isValidLrc(lyricContent)) {
             return lyricContent;
        }
        return null;
    }

    private String doGet(String url) {
        List<HttpCookie> cookies;
        try (HttpResponse response = HttpRequest.get(url).timeout(HttpGlobalConfig.getTimeout()).execute()) {
            if (response.isOk()) {
                return response.body();
            }
            cookies = response.getCookies();
        }

        if (CollUtil.isEmpty(cookies)) {
            return null;
        }

        try (HttpResponse response = HttpRequest.get(url)
                .timeout(HttpGlobalConfig.getTimeout())
                .cookie(cookies)
                .execute()) {
            if (response.isOk()) {
                return response.body();
            }
            return null;
        }
    }

    private String search(String musicName, String musicArtist) {
        String url = HOST + "/search.php?q=" + musicName;
        String searchContent = doGet(url);
        if (StrUtil.isBlank(searchContent)) {
            return null;
        }
        Document doc = Jsoup.parse(searchContent);
        Elements its = doc.body().getElementsByClass("song-card");
        for (Element it : its) {
            String title = it.getElementsByClass("song-title").text();
            String artist = it.getElementsByClass("artist-link").text();
            String album = it.getElementsByClass("album-link").text();
            String lyricLink = it.getElementsByClass("song-link").attr("href");

//            System.out.printf("title: %s, artist: %s, album: %s, lyricLink: %s\n", title, artist, album, lyricLink);

            if (Comm.compare(title, musicName) && Comm.compare(artist, musicArtist)) {
                return lyricLink;
            }
        }
        return null;
    }

    private String getByLyricLink(String lyricLink) {
        String content = doGet(HOST + lyricLink);
        Document doc = Jsoup.parse(content);
        Element it = doc.body()
                .getElementById("lrc-lyric-desktop")
                .getElementsByClass("lyric-content")
                .first();
        return it.text();
    }

    public static void main(String[] args) {
//        XiaoJiangClubProvider s = new XiaoJiangClubProvider();
//        String lyric = s.getLyric("无数", "薛之谦");
//        System.out.println(lyric);

        System.out.println(StrUtil.containsIgnoreCase("GEM邓紫棋", "邓紫棋"));
        System.out.println(StrUtil.containsIgnoreCase("asdfDsdfSDF", "dfs"));
    }
}
