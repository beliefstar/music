package com.zx.music.controller;


import com.zx.music.lyric.LyricService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xzhen
 * @creatAt 2026-02-25 17:44
 */
@RestController
@RequestMapping("/api/v2/lyric")
public class LyricController {

    @Autowired
    private LyricService lyricService;

    @GetMapping
    public String getLyric(String musicId) {
        return lyricService.getLyric(musicId);
    }
}
