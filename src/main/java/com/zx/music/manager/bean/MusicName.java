package com.zx.music.manager.bean;


import cn.hutool.core.codec.Base64;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Data;

/**
 * @author xzhen
 * @creatAt 2025-08-27 14:31
 */
@Data
public class MusicName {

    private String musicId;

    private String name;

    private String artist;

    private String ext;

    public static MusicName of(String musicId) {
        if (StrUtil.isBlank(musicId)) {
            return null;
        }
        String ext = FileNameUtil.extName(musicId);
        String main = FileNameUtil.mainName(musicId);

        String name = Base64.decodeStr(main);

        MusicName musicName = new MusicName();
        musicName.setMusicId(musicId);
        musicName.setName(name);
        musicName.setExt(ext);
        parseArtist(musicName);
        return musicName;
    }


    public static MusicName ofRawName(String name) {
        if (StrUtil.isBlank(name)) {
            return null;
        }
        String ext = FileNameUtil.extName(name);
        String main = FileNameUtil.mainName(name);

        String musicId = Base64.encodeUrlSafe(main) + "." + ext;

        MusicName musicName = new MusicName();
        musicName.setMusicId(musicId);
        musicName.setName(main);
        musicName.setExt(ext);
        parseArtist(musicName);
        return musicName;
    }

    private static void parseArtist(MusicName musicName) {
        musicName.setArtist("");
        String name = musicName.getName();

        if (name.contains("《") && name.contains("》")) {
            int l = name.indexOf('《');
            int r = name.indexOf('》');
            if (l < r) {
                String artist = name.substring(0, l).trim();
                String title = name.substring(l + 1, r).trim();

                musicName.setName(title);
                musicName.setArtist(artist);
            }
        }

        if (name.contains("-")) {
            String[] parts = name.split("-");
            if (parts.length >= 2) {
                String artist = parts[0].trim();
                String part1 = parts[1];
                if (part1.contains(".")) {
                    part1 = part1.substring(0, part1.lastIndexOf('.'));
                }
                String title = part1.trim();
                musicName.setName(title);
                musicName.setArtist(artist);
            }
        }
    }

    public String decode() {
        return name + artist + "." + ext;
    }
}
