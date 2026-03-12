package com.zx.music.provider.impl;


import cn.hutool.core.codec.Base64Decoder;
import cn.hutool.core.codec.Base64Encoder;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.zx.music.bean.SearchResult;
import com.zx.music.manager.bean.MusicName;
import com.zx.music.provider.MusicProvider;
import com.zx.music.util.Comm;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xzhen
 * @creatAt 2026-03-12 10:16
 */
@Component
public class GequhaiProvider implements MusicProvider {

    public static final String HOST = "https://www.gequhai.com";


    @Override
    public List<SearchResult> search(String keyword) {
        List<SearchResult> results = new ArrayList<>();
        String url = HOST + "/s/" + URLUtil.encode(keyword);

        String content = HttpUtil.get(url);
        Document doc = Jsoup.parse(content);
        Element table = doc.body().getElementById("myTables");
        Element tbody = table.getElementsByTag("tbody").get(0);

        Elements tr = tbody.getElementsByTag("tr");
        for (Element it : tr) {
            try {
                Elements td = it.getElementsByTag("td");
                if (td.size() < 3) {
                    continue;
                }
                Element a = td.get(1).getElementsByTag("a").get(0);
                String id = a.attr("href");
                String name = a.text();
                String artist = td.get(2).text();

                SearchResult sr = new SearchResult();
                sr.setId(id);
                sr.setName(name);
                sr.setArtist(artist);
                sr.setExt("mp3");

                sr.setId(Base64Encoder.encodeUrlSafe(JSON.toJSONString(sr)));
                results.add(sr);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return results;
    }


    @Override
    public String touch(String id) {
        SearchResult sr = JSON.parseObject(Base64Decoder.decodeStr(id), SearchResult.class);
        String url = HOST + sr.getId();
        String content = HttpUtil.get(url);

        String playId = null;
        List<String> play_ids = ReUtil.findAllGroup0("play_id.*?;", content);
        if (CollUtil.isNotEmpty(play_ids)) {
            String s = play_ids.get(0);
            List<String> parts = StrUtil.split(s, "=", true, true);
            String val = parts.get(1).trim();
            if (val.endsWith(";")) {
                val = val.substring(0, val.length() - 1);
            }
            playId = StrUtil.strip(val, "'");
        }

        String mp3Type = null;
        List<String> mp3Types = ReUtil.findAllGroup0("mp3_type.*?;", content);
        if (CollUtil.isNotEmpty(mp3Types)) {
            String s = mp3Types.get(0);
            List<String> parts = StrUtil.split(s, "=", true, true);
            String val = parts.get(1).trim();
            if (val.endsWith(";")) {
                val = val.substring(0, val.length() - 1);
            }
            mp3Type = val;
        }

        if (StrUtil.isBlank(playId) || StrUtil.isBlank(mp3Type)) {
            return null;
        }

        String touchUrl = HOST + "/api/music";
        String downloadUrl;
        try (HttpResponse response = HttpRequest.post(touchUrl)
                .form("id", playId)
                .form("type", mp3Type)
                .header("X-Requested-With", "XMLHttpRequest")
                .header("X-Custom-Header", "SecretKey")
                .execute()) {
            JSONObject jo = JSON.parseObject(response.body());
            downloadUrl = jo.getJSONObject("data").getString("url");
        }

        if (StrUtil.isBlank(downloadUrl)) {
            return null;
        }

        String ext = FileNameUtil.extName(downloadUrl);

        MusicName musicName = MusicName.of(sr.getName(), sr.getArtist(), ext);
        String musicId = musicName.getMusicId();
        Comm.download(downloadUrl, musicId);
        return musicId;
    }
}
