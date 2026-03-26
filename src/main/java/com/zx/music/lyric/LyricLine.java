package com.zx.music.lyric;


public class LyricLine {
    public long timeMs;
    public String text;

    public LyricLine(long t, String s) {
        timeMs = t;
        text = s;
    }

    @Override
    public String toString() {
        return "{" +
                "timeMs=" + timeMs +
                ", text='" + text + '\'' +
                '}';
    }
}
