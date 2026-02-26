package com.zx.music.album;


import cn.hutool.core.codec.Base64;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpGlobalConfig;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.zx.music.manager.bean.MusicName;
import com.zx.music.util.AsyncExecutor;
import com.zx.music.util.Comm;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xzhen
 * @creatAt 2026-02-26 15:47
 */
@Service
public class AlbumService {

    private Map<String, String> musicAlbumMap;

    private Map<String, AlbumData> albumMap;


    private final Cache<String, Long> cache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(Duration.ofHours(2))
            .build();

    @PostConstruct
    public void loadData() {
        File albumDir = Comm.getAlbumDir();
        File albumDataFile = FileUtil.touch(new File(albumDir, "album.data"));

        String albumData = FileUtil.readString(albumDataFile, StandardCharsets.UTF_8);
        if (StrUtil.isBlank(albumData)) {
            albumData = "[]";
        }
        List<AlbumData> as = JSON.parseArray(albumData, AlbumData.class);
        Map<String, String> map = new ConcurrentHashMap<>();
        Map<String, AlbumData> map2 = new ConcurrentHashMap<>();

        for (AlbumData data : as) {
            map2.put(data.getAlbum(), data);
            for (String musicId : data.getMusics()) {
                map.put(musicId, data.getAlbum());
            }
        }

        this.musicAlbumMap = map;
        this.albumMap = map2;
    }

    public String getAlbum(String musicId) {
        return musicAlbumMap.get(musicId);
    }

    public void updateMusicAlbum(String musicId, String album) {
        AlbumData albumData = albumMap.get(album);
        if (albumData == null) {
            albumData = new AlbumData();
            albumData.setAlbum(album);
            albumData.setMusics(new ArrayList<>());

            albumMap.put(album, albumData);
        }

        if (!albumData.getMusics().contains(musicId)) {
            albumData.getMusics().add(musicId);
        }

        musicAlbumMap.put(musicId, album);

        AsyncExecutor.execute(() -> {
            File albumDir = Comm.getAlbumDir();
            File albumDataFile = FileUtil.touch(new File(albumDir, "album.data"));

            List<AlbumData> as = new ArrayList<>(albumMap.values());
            String albumDataStr = JSON.toJSONString(as);
            FileUtil.writeString(albumDataStr, albumDataFile, StandardCharsets.UTF_8);
        });
    }

    public File getAlbumImage(String musicId) {
        File file = loadImageFromDisk(musicId);
        if (file != null && file.exists()) {
            return file;
        }

        if (!check(musicId)) {
            return null;
        }

        return loadImageFromSearch(musicId);
    }

    private synchronized boolean check(String musicId) {
        Long limit = cache.getIfPresent(musicId);
        if (limit != null) {
            return false;
        }
        cache.put(musicId, System.currentTimeMillis());
        return true;
    }

    private File loadImageFromSearch(String musicId) {
        // GEThttps://itunes.apple.com/search?term=%E6%9A%82%E6%97%B6%E7%9A%84%E8%AE%B0%E5%8F%B7&media=music&entity=song&country=cn&limit=50
        MusicName musicName = MusicName.of(musicId);
        if (musicName == null) {
            return null;
        }

        String url = "https://itunes.apple.com/search?term=" + musicName.getName() + "&media=music&entity=song&country=cn&limit=10";
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

                if (StrUtil.equalsIgnoreCase(artistName, musicName.getArtist())) {
                    updateMusicAlbum(musicId, albumName);

                    imageUrl = resize(imageUrl);

                    File file = new File(Comm.getAlbumImageDir(), Base64.encodeUrlSafe(albumName) + ".jpg");
                    try (HttpResponse imageResponse = HttpRequest.get(imageUrl).timeout(HttpGlobalConfig.getTimeout()).execute()) {
                        if (!imageResponse.isOk()) {
                            return null;
                        }
                        FileUtil.writeBytes(imageResponse.bodyBytes(), file);
                        return file;
                    } catch (Exception e) {
                        return null;
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

    private File loadImageFromDisk(String musicId) {
        String album = getAlbum(musicId);
        if (StrUtil.isBlank(album)) {
            return null;
        }
        File albumDir = Comm.getAlbumImageDir();
        String fileName = Base64.encodeUrlSafe(album) + ".jpg";
        File file = new File(albumDir, fileName);
        if (!file.exists()) {
            return null;
        }
        return file;
    }
}
