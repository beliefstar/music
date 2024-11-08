package com.zx.music.util;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpGlobalConfig;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.zx.music.bean.SearchItem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Comm {

    public static final String MUSIC_NAME_SUFFIX = ".mp3";

    public static String parsePlayUrlPhp(String url) {
        url = StrUtil.trim(url);
        String[] split = url.split("\\+");
        String prefix = Optional.ofNullable(split[0]).map(String::trim).map(t -> t.substring(0, t.length() - 1)).orElse("");

        List<String> titles = ReUtil.findAll("generateParam\\('(.*?)'\\)", split[1], 1);
        String key = titles.get(0);
        key = Encryptor.generateParam(key);
        return "https://www.hifini.com/" + prefix + key;
    }

    public static String parsePlayUrl(String s) {
        if (s.contains("get_music.php")) {
            List<String> titles = ReUtil.findAll("url: '(get_music\\.php.*?)pic", s, 1);
            return parsePlayUrlPhp(titles.get(0));
        }
        JSONArray ja = JSON.parseArray("[" + s + "]");
        JSONObject jo = ja.getJSONObject(0);
        return jo.getString("url");
    }

    public static void download(String url, String id) {
        System.out.println(url);
        HttpGlobalConfig.setMaxRedirectCount(999);
        try (HttpResponse response = HttpRequest.get(url).timeout(60000)
                .header("Referer", "https://www.hifini.com/")
                .setFollowRedirects(true)
                .setMaxRedirectCount(999)
                .execute()) {
            storeFile(response.bodyStream(), id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void storeFile(InputStream in, String id) {
        try (FileOutputStream out = new FileOutputStream("store/" + id)) {
            IoUtil.copy(in, out);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String buildStoreId(String name) {
        return Base64.encodeUrlSafe(name) + MUSIC_NAME_SUFFIX;
    }

    public static boolean exists(String id) {
        return new File("store/" + id).exists();
    }

    public static File storeFile(String id) {
        return new File("store/" + id);
    }

    public static List<SearchItem> search(String keyword, int page) {
        String host = "https://www.hifini.com";
        String range = "1";
        keyword = URLUtil.encode(keyword).replace("%", "_");
        String url = StrUtil.format("{}/search-{}-{}-{}.htm", host, keyword, range, page);

        String listContent = HttpUtil.get(url);
        List<String> titles = ReUtil.findAll("(<a href=\"thread-\\w+.htm\">.*?</a>)", listContent, 1);

        List<SearchItem> items = new ArrayList<>();
        if (CollUtil.isNotEmpty(titles)) {
            for (String title : titles) {
                SearchItem si = parseSearchItem(title);
                if (si != null) {
                    items.add(si);
                }
            }
        }
        return items;
    }



    public static SearchItem parseSearchItem(String it) {
        String ref = extractRef(it);
        String name = extractTitle(it);
        if (StrUtil.isBlank(ref) || StrUtil.isBlank(name)) {
            System.out.println(it);
            return null;
        }
        String id = Comm.buildStoreId(name);
        if (exists(id)) {
            return null;
        }
        SearchItem si = new SearchItem();
        si.setId(id);
        si.setName(name + MUSIC_NAME_SUFFIX);
        si.setThread(ref);
        si.setCache(false);
        return si;
    }

    private static String extractRef(String s) {
        List<String> titles = ReUtil.findAll("<a href=\"(thread-\\w+.htm)\">.*?</a>", s, 1);
        for (String title : titles) {
            title = title.replace("<em>", "");
            title = title.replace("</em>", "");
            title = title.trim();
            return title;
        }
        return "";
    }
    private static String extractTitle(String s) {
        List<String> titles = ReUtil.findAll("<a href=\"thread-\\w+.htm\">(.*?)(\\[.*?])?</a>", s, 1);
        for (String title : titles) {
            title = title.replace("<em>", "");
            title = title.replace("</em>", "");
            title = title.trim();
            return title;
        }
        return "";
    }

    public static void main(String[] args) throws FileNotFoundException {
//        String s = "https://m.hifini.com/music/苏打绿 - 我好想你.m4a";
//        String s = "https://www.hifini.com/" + parsePlayUrl("get_music.php?key=wwwHiFiNicomHyGdaqX6xxHiFiNixxS3DdjAZKGbFgt2c9mouzAECUCuvLREzdL8GG88qUPRxcUTa50ylNt0&p='+ generateParam('Ypn4ARo65f0TNUO6IseHoTLbQ6YFnfPV7DQbMfx7P1fey2lJHAoyKrMNDLwobnvyu0uocNgQN8IRL6JGxV4klQ')");
        String s = "https://www.hifini.com/get_music.php?key=r8UmKewwwHiFiNicom55Q0THpHBHW6YSrXfiZ1FM3pooGSxIuexxHiFiNixxpOk0VqCwwwwHiFiNicombE4QwGVWUw&p=O5ZAAEJUFYASEGJ6BYNAAHKFIBHH2HRRDYDAAJIGEMRTAFQ5MZ5AY5RBDESXUH3VDEVQWGYXEVNXCUD3DYHQ6PIKCQWBWPIVEQSAKW3NPYSQENI2AMQAQIJ6EEWCSXCENZLS2IQUBMPDCHQGAASQMI23KRLFQOQZJ4YBCDQABAAC2BQVJJ5VGTJOCIZBEBL6";

        System.out.println(s);

        HttpResponse response = HttpRequest.get(s).timeout(60000)
                .header("Referer", "https://www.hifini.com/")
                .setFollowRedirects(true)
                .execute();
        FileOutputStream out = new FileOutputStream("/Users/yuanqinghuihong/Desktop/work/debug/test3.mp3");
        IoUtil.copy(response.bodyStream(), out);
        IoUtil.close(out);
    }
}
