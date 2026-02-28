package com.zx.music.album.provider;


import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.zx.music.album.AlbumProvider;
import com.zx.music.manager.bean.MusicName;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.function.Function;

/**
 * @author xzhen
 * @creatAt 2026-02-28 17:28
 */
@Component
@Order(2)
public class AlbumCxProvider implements AlbumProvider {

    public static final String BASE_URL = "https://api.lrc.cx/cover";
    @Override
    public File getAlbumImage(String musicId, String album, Function<String, File> consumer) {
        MusicName musicName = MusicName.of(musicId);
        if (musicName == null) {
            return null;
        }

        String url = StrUtil.format("{}?title={}&artist={}&album={}", BASE_URL, musicName.getName(), musicName.getArtist(), StrUtil.nullToEmpty(album));
        try (HttpResponse response = HttpRequest.get(url)
                .setMaxRedirectCount(999)
                .timeout(60_000)
                .execute()) {
            if (response.isOk()) {
                File apply = consumer.apply(null);
                FileUtil.writeFromStream(response.bodyStream(), apply);
                return apply;
            }
            return null;
        }
    }
}
