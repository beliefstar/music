package com.zx.music.album;


import cn.hutool.core.codec.Base64;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.zx.music.util.AsyncExecutor;
import com.zx.music.util.Comm;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    @Autowired
    private List<AlbumProvider> albumProviders;

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

        return loadFromProvider(musicId);
    }

    private synchronized boolean check(String musicId) {
        Long limit = cache.getIfPresent(musicId);
        if (limit != null) {
            return false;
        }
        cache.put(musicId, System.currentTimeMillis());
        return true;
    }

    private File loadFromProvider(String musicId) {
        for (AlbumProvider provider : albumProviders) {
            File f = provider.getAlbumImage(musicId, getAlbum(musicId), albumName -> {
                if (StrUtil.isBlank(albumName)) {
                    String mainName = FileUtil.mainName(musicId);
                    return new File(Comm.getAlbumImageDir(), mainName + ".jpg");
                }
                updateMusicAlbum(musicId, albumName);
                return new File(Comm.getAlbumImageDir(), Base64.encodeUrlSafe(albumName) + ".jpg");
            });
            if (f != null) {
                return f;
            }
        }
        return null;
    }

    private File loadImageFromDisk(String musicId) {
        String album = getAlbum(musicId);

        String mainName;
        if (StrUtil.isBlank(album)) {
            mainName = FileUtil.mainName(musicId);
        } else {
            mainName = Base64.encodeUrlSafe(album);
        }

        File albumDir = Comm.getAlbumImageDir();
        String fileName = mainName + ".jpg";
        File file = new File(albumDir, fileName);
        if (!file.exists()) {
            return null;
        }
        return file;
    }
}
