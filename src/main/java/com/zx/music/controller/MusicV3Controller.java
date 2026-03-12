package com.zx.music.controller;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.zx.music.bean.MusicItem;
import com.zx.music.bean.SearchResult;
import com.zx.music.manager.MusicManager;
import com.zx.music.provider.MusicProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xzhen
 * @creatAt 2026-03-12 11:20
 */
@RestController
@RequestMapping("/api/v3/music")
public class MusicV3Controller {

    @Autowired
    private MusicManager musicManager;

    @Autowired
    private MusicProvider musicProvider;

    @GetMapping("/search")
    public List<SearchResult> search(String keyword) {
        List<MusicItem> musicItems = musicManager.getMusicItems(keyword);
        List<SearchResult> items = musicProvider.search(keyword);
        if (CollUtil.isNotEmpty(musicItems)) {
            List<SearchResult> result = new ArrayList<>();
            for (MusicItem item : musicItems) {
                SearchResult si = new SearchResult();
                si.setId(item.getId());
                si.setName(item.getName());
                si.setArtist(item.getArtist());
                si.setExt(item.getExt());
                result.add(si);
            }
            for (SearchResult item : items) {
                if (musicManager.exists(item.getName(), item.getArtist())) {
                    continue;
                }
                result.add(item);
            }
            items = result;
        }
        return items;
    }

    @GetMapping("/touch")
    public String touch(String id) {
        if (musicManager.exists(id)) {
            return id;
        }
        String musicId = musicProvider.touch(id);
        if (StrUtil.isNotBlank(musicId)) {
            musicManager.init();
        }
        return musicId;
    }
}
