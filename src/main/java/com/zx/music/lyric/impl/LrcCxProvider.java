package com.zx.music.lyric.impl;


import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpGlobalConfig;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.zx.music.lyric.LrcValidator;
import com.zx.music.lyric.LyricProvider;
import com.zx.music.manager.bean.MusicName;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

/**
 * @author xzhen
 * @creatAt 2026-02-28 16:28
 */
@Service
@Order(1)
public class LrcCxProvider implements LyricProvider {
    public static final String BASE_URL = "https://api.lrc.cx/lyrics";
    @Override
    public String getLyric(String musicName, String musicArtist) {
        String lyric = fetch(musicName, musicArtist);
        if (StrUtil.isBlank(lyric)) {
            return null;
        }

        if (LrcValidator.isValidLrc(lyric)) {
            return lyric;
        }
        return null;
    }

    private String fetch(String name, String artist) {
        String url = StrUtil.format("{}?title={}&artist={}", BASE_URL, name, artist);
        try (HttpResponse response = HttpRequest.get(url)
                .timeout(HttpGlobalConfig.getTimeout())
                .execute()) {
            if (response.isOk()) {
                return response.body();
            }
            return null;
        }
    }

    public static void main(String[] args) {
        String s = "6Zi_5qKo57Kk44CK5ZC05ZOl56qf44CL.mp3";
        MusicName mn = MusicName.of(s);

        LrcCxProvider provider = new LrcCxProvider();
        String lyric = provider.getLyric(mn.getName(), mn.getArtist());
        System.out.println(lyric);
    }
}
