package com.zx.music.lyric;


import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.zx.music.manager.bean.MusicName;
import com.zx.music.util.Comm;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.List;

/**
 * @author xzhen
 * @creatAt 2026-02-26 09:44
 */
@Service
public class LyricService {

    private final List<LyricProvider> lyricProviders;

    public LyricService(List<LyricProvider> lyricProviders) {
        this.lyricProviders = lyricProviders;
    }

    public String getLyric(String musicId) {
        MusicName musicName = MusicName.of(musicId);
        if (musicName == null) {
            return null;
        }

        File lyricFile = Comm.getLyricFile(musicId);
        if (lyricFile.exists()) {
            return FileUtil.readString(lyricFile, StandardCharsets.UTF_8);
        }
        File lyricTempFile = Comm.getLyricTempFile(musicId);
        if (lyricTempFile.exists()) {
            String tempContent = FileUtil.readString(lyricTempFile, StandardCharsets.UTF_8);
            if (StrUtil.isBlank(tempContent)) {
                FileUtil.writeString(DateUtil.now(), tempContent, StandardCharsets.UTF_8);
                return null;
            }
            Date lastTime = DateUtil.parse(tempContent);
            if (System.currentTimeMillis() - lastTime.getTime() < Duration.ofHours(2).toMillis()) {
                return null;
            }
            FileUtil.del(lyricTempFile);
        }

        String lyric = getLyricFromProvider(musicName);
        if (StrUtil.isBlank(lyric)) {
            FileUtil.writeString(DateUtil.now(), lyricTempFile, StandardCharsets.UTF_8);
            return null;
        }
        FileUtil.writeString(lyric, lyricFile, StandardCharsets.UTF_8);
        return lyric;
    }

    private String getLyricFromProvider(MusicName musicName) {
        try {
            for (LyricProvider lyricProvider : lyricProviders) {
                String lyric = lyricProvider.getLyric(musicName.getName(), musicName.getArtist());
                if (StrUtil.isNotBlank(lyric)) {
                    return lyric;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
