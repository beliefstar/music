package com.zx.music.common;


import lombok.Getter;

/**
 * @author xzhen
 * @creatAt 2025-08-27 14:26
 */
@Getter
public enum MusicExt {

    MP3("mp3"),
    WAV("wav"),

    ;
    private final String ext;
    MusicExt(String ext) {
        this.ext = ext;
    }

    public static boolean contains(String ext) {
        for (MusicExt musicExt : values()) {
            if (musicExt.getExt().equalsIgnoreCase(ext)) {
                return true;
            }
        }
        return false;
    }
}
