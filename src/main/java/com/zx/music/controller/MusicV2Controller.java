package com.zx.music.controller;

import cn.hutool.core.collection.CollUtil;
import com.zx.music.bean.MusicItem;
import com.zx.music.bean.SearchItem;
import com.zx.music.manager.MusicManager;
import com.zx.music.util.Comm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/v2/music")
public class MusicV2Controller {

    @Autowired
    private MusicManager musicManager;


    @GetMapping(value = "/resource/{id}", produces = "audio/mpeg")
    public ResponseEntity<Resource> playResource(@PathVariable("id") String id, HttpServletResponse response) {
        response.addHeader(HttpHeaders.CACHE_CONTROL,
                CacheControl.maxAge(Duration.ofDays(365)).cachePublic().getHeaderValue());
        return ResponseEntity.ok().body(new FileSystemResource("store/" + id));
    }


    @GetMapping("/search")
    public List<SearchItem> search(String keyword,
                                   @RequestParam(value = "page", defaultValue = "1") Integer page) {
        List<MusicItem> musicItems = musicManager.getMusicItems(keyword);
        List<SearchItem> items = Comm.search(keyword, page);
        if (CollUtil.isNotEmpty(musicItems)) {
            List<SearchItem> result = new ArrayList<>();
            for (MusicItem item : musicItems) {
                SearchItem si = new SearchItem();
                si.setId(item.getId());
                si.setName(item.getName());
                si.setCache(true);
                result.add(si);
            }
            for (SearchItem item : items) {
                if (musicManager.exists(item.getName())) {
                    continue;
                }
                result.add(item);
            }
            items = result;
        }
        return items;
    }

    @GetMapping("/delete")
    public Boolean delete(String id) {
        musicManager.remove(id);
        return new File("store/" + id).delete();
    }

    @PostMapping("/upload/{id}")
    public void upload(@PathVariable String id, HttpServletRequest req) throws IOException {
        File file = Comm.storeFile(id);
        if (file.exists() && file.length() >= req.getContentLengthLong()) {
            return;
        }
        Comm.storeFile(req.getInputStream(), id);
        musicManager.put(id, new Date());
    }
}
