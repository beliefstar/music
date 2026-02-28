package com.zx.music.album.provider;


import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpGlobalConfig;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.zx.music.album.AlbumProvider;
import com.zx.music.manager.bean.MusicName;
import com.zx.music.util.Comm;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.function.Function;

/**
 * @author xzhen
 * @creatAt 2026-02-28 17:06
 */
@Service
@Order(1)
public class AppleAlbumProvider implements AlbumProvider {

    @Override
    public File getAlbumImage(String musicId, String album, Function<String, File> consumer) {
        MusicName musicName = MusicName.of(musicId);
        if (musicName == null) {
            return null;
        }

        String key = StrUtil.isBlank(album) ? musicName.getName() : album;
        String url = "https://itunes.apple.com/search?term=" + key + "&media=music&entity=song&country=cn&limit=10";
        try (HttpResponse response = HttpRequest.get(url).timeout(HttpGlobalConfig.getTimeout()).execute()) {
            if (!response.isOk()) {
                return null;
            }
            JSONObject jo = JSON.parseObject(response.body());
            if (jo.getIntValue("resultCount") <= 0) {
                return null;
            }
            JSONArray results = jo.getJSONArray("results");
            for (int i = 0; i < results.size(); i++) {
                JSONObject it = results.getJSONObject(i);
                String trackName = it.getString("trackName");
                String artistName = it.getString("artistName");
                String albumName = it.getString("collectionName");
                String imageUrl = it.getString("artworkUrl100");

                if (Comm.compare(artistName, musicName.getArtist())) {
                    imageUrl = resize(imageUrl);
                    try (HttpResponse imageResponse = HttpRequest.get(imageUrl).timeout(HttpGlobalConfig.getTimeout()).execute()) {
                        if (!imageResponse.isOk()) {
                            return null;
                        }
                        File apply = consumer.apply(albumName);
                        FileUtil.writeFromStream(imageResponse.bodyStream(), apply);
                        return apply;
                    } catch (Exception ignore) {
                    }
                }
            }
        }
        return null;
    }

    private String resize(String imageUrl) {
        if (StrUtil.isBlank(imageUrl)) {
            return imageUrl;
        }
        return imageUrl.replace("100x100", "600x600");
    }
}
