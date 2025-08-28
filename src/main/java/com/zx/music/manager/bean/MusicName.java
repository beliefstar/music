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
        return musicName;
    }


    public static MusicName ofName(String name) {
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
        return musicName;
    }

    public String decode() {
        return name + "." + ext;
    }
}
