package com.zx.music.controller;


import com.zx.music.album.AlbumService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.time.Duration;

/**
 * @author xzhen
 * @creatAt 2026-02-25 17:44
 */
@RestController
@RequestMapping("/api/v2/album")
public class AlbumController {

    @Autowired
    private AlbumService albumService;

    @GetMapping(value = "/image", produces = "image/jpeg")
    public ResponseEntity<Resource> getAlbumImage(String musicId, HttpServletResponse response) {
        File file = albumService.getAlbumImage(musicId);
        if (file == null || !file.exists()) {
            return ResponseEntity.notFound().build();
        }
        response.addHeader(HttpHeaders.CACHE_CONTROL,
                CacheControl.maxAge(Duration.ofDays(365)).cachePublic().getHeaderValue());
        return ResponseEntity.ok().body(new FileSystemResource(file));
    }

    @GetMapping("/name")
    public String getAlbum(String musicId) {
        return albumService.getAlbum(musicId);
    }
}
