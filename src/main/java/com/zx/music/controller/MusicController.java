package com.zx.music.controller;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpUtil;
import com.zx.music.bean.MusicItem;
import com.zx.music.manager.MusicManager;
import com.zx.music.util.AsyncExecutor;
import com.zx.music.util.Comm;
import com.zx.music.util.Constants;
import com.zx.music.util.ResultListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Semaphore;

@RestController
@RequestMapping("/api/music")
public class MusicController {

    @Autowired
    private MusicManager musicManager;

    private final Semaphore touchSemaphore = new Semaphore(1);

    private final ResultListener<String> resultListener = new ResultListener<>();

    @GetMapping("/list")
    public List<MusicItem> list() {
        return musicManager.getMusicItems();
    }

    @GetMapping("/play")
    public Resource play(String id, HttpServletResponse response) {
        response.addHeader(HttpHeaders.CACHE_CONTROL,
                CacheControl.maxAge(Duration.ofDays(365)).cachePublic().getHeaderValue());
        return new FileSystemResource("store/" + id);
    }

    @GetMapping(value = "/resource/{id}", produces = "audio/mpeg")
    public ResponseEntity<Resource> playResource(@PathVariable("id") String id, HttpServletResponse response) {
        response.addHeader(HttpHeaders.CACHE_CONTROL,
                CacheControl.maxAge(Duration.ofDays(365)).cachePublic().getHeaderValue());
        return ResponseEntity.ok().body(new FileSystemResource("store/" + id));
    }


    @GetMapping("/search")
    public List<String> search(String keyword,
                              @RequestParam(value = "page", defaultValue = "1") Integer page) {

        String host = "https://www.hifini.com";
        String range = "1";
        keyword = URLUtil.encode(keyword).replace("%", "_");
        String url = StrUtil.format("{}/search-{}-{}-{}.htm", host, keyword, range, page);

        String listContent = HttpUtil.get(url);
        List<String> titles = ReUtil.findAll("(<a href=\"thread-\\w+.htm\">.*?</a>)", listContent, 1);
        return titles;
    }

    @GetMapping("/getPlayUrl")
    public DeferredResult<String> getPlayUrl(String thread, String name) {
        DeferredResult<String> deferredResult = new DeferredResult<>(60000L, () -> Constants.SERVICE_TIMEOUT);
        String id = Comm.buildStoreId(name);
        if (Comm.exists(id)) {
            deferredResult.setResult(id);
            return deferredResult;
        }
        boolean register = resultListener.register(id, (r, ex) -> {
            if (ex != null) {
                deferredResult.setResult(Constants.SERVICE_ERROR);
                return;
            }
            deferredResult.setResult(r);
        });
        if (!register) {
            return deferredResult;
        }
        AsyncExecutor.supply(() -> {
            if (!touchSemaphore.tryAcquire()) {
                return Constants.SERVICE_BUSY;
            }
            try {
                String url = "https://www.hifini.com/" + thread;
                String listContent = HttpUtil.get(url);
                List<String> titles = ReUtil.findAll("music: \\[(.*?)\\]", listContent, 1);
                String playUrl;
                try {
                    playUrl = Comm.parsePlayUrl(titles.get(0));
                } catch (Exception e) {
                    return Constants.SERVICE_ERROR;
                }
                if (StrUtil.isBlank(playUrl)) {
                    return Constants.SERVICE_ERROR;
                }
                Comm.download(playUrl, id);
                musicManager.put(id, new Date());
                return id;
            } finally {
                touchSemaphore.release();
            }
        }).whenComplete((r, ex) -> {
            resultListener.release(id, r);
            if (ex != null) {
                deferredResult.setResult(Constants.SERVICE_ERROR);
                return;
            }
            deferredResult.setResult(r);
        });

        return deferredResult;
    }


    @GetMapping("/delete")
    public Boolean delete(String id) {
        musicManager.remove(id);
        return new File("store/" + id).delete();
    }

    @PostMapping("/upload/{id}")
    public void upload(@PathVariable String id, HttpServletRequest req) throws IOException {
        if (Comm.exists(id)) {
            return;
        }
        Comm.storeFile(req.getInputStream(), id);
    }
}
