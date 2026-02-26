package com.zx.music.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.zx.music.bean.MusicItem;
import com.zx.music.bean.SearchItem;
import com.zx.music.manager.MusicManager;
import com.zx.music.manager.bean.MusicName;
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
import org.springframework.web.multipart.MultipartFile;

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
        return ResponseEntity.ok().body(new FileSystemResource(Comm.getMusicFile(id)));
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
                si.setName(StrUtil.format("{}-{}.{}", item.getArtist(), item.getName(), item.getExt()));
                si.setCache(true);
                result.add(si);
            }
            for (SearchItem item : items) {
                if (musicManager.exists(item.getId())) {
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
        return Comm.getMusicFile(id).delete();
    }

    @PostMapping("/upload/{id}")
    public void upload(@PathVariable String id, HttpServletRequest req) throws IOException {
        File file = Comm.getMusicFile(id);
        if (file.exists()) {
            if (file.length() >= req.getContentLengthLong()) {
                return;
            }
            file.delete();
        }
        Comm.storeMusicFile(req.getInputStream(), id);
        musicManager.put(id, new Date());
    }


    @PostMapping("/upload/multi")
    public void uploadMulti(MultipartFile file) throws IOException {
        MusicName musicName = MusicName.ofRawName(file.getOriginalFilename());
        if (musicName == null) {
            return;
        }
        File diskFile = Comm.getMusicFile(musicName.getMusicId());
        if (diskFile.exists()) {
            diskFile.delete();
        }
        Comm.storeMusicFile(file.getInputStream(), musicName.getMusicId());
        musicManager.put(musicName.getMusicId(), new Date());
    }
}
