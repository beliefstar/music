package com.zx.music.util;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.zx.music.bean.SearchItem;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.*;

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
        System.out.println("download: " + url);
        try (HttpResponse response = HttpRequest.get(url).timeout(60000)
                .setFollowRedirects(false)
                .execute()) {

            if (response.getStatus() == 302) {
                String location = response.header("location");
                System.out.println("302: " + location);
                download(location, id);
                return;
            }

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

    public static List<Element> search_raw(String keyword, int page) {
        String host = "https://www.hifini.com";
        String range = "1";
        keyword = URLUtil.encode(keyword).replace("%", "_");
        String url = StrUtil.format("{}/search-{}-{}-{}.htm", host, keyword, range, page);

        String listContent = HttpUtil.get(url);
        Document doc = Jsoup.parse(listContent);
        Elements as = doc.body().getElementsByTag("a");

        List<Element> list = new ArrayList<>();
        for (Element a : as) {
            String ref = a.attr("href");
            if (ref.startsWith("thread-")) {
                list.add(a);
            }
        }
        return list;
    }
    public static List<SearchItem> search(String keyword, int page) {
        List<Element> as = search_raw(keyword, page);

        List<SearchItem> list = new ArrayList<>();
        for (Element a : as) {
            String ref = a.attr("href");
            String name = parseName(a.text());
            if (StrUtil.isBlank(ref) || StrUtil.isBlank(name)) {
                System.out.println(a);
                continue;
            }
            String id = Comm.buildStoreId(name);
            if (exists(id)) {
                continue;
            }
            SearchItem si = new SearchItem();
            si.setId(id);
            si.setName(name + MUSIC_NAME_SUFFIX);
            si.setThread(ref);
            si.setCache(false);
            list.add(si);
        }
        return list;
    }

    public static List<SearchItem> _search(String keyword, int page) {
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


    private static String parseName(String name) {
        int lidx = name.indexOf("[");
        int ridx = name.lastIndexOf("]");
        if (lidx == -1 || ridx == -1) {
            return name;
        }
        return name.substring(0, lidx) + name.substring(ridx + 1);
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
        List<String> titles = ReUtil.findAll("<a href=\"(thread-\\w+.htm)\".*?</a>", s, 1);
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

    public static boolean validBbsToken(String bbsToken) {
        HttpRequest request = HttpRequest.get("https://www.hifini.com/my.htm")
                .header("Cookie", "bbs_token=" + bbsToken);

        try (HttpResponse response = request.execute()) {
            return response.getStatus() == 200;
        }
    }

    public static String queryDownloadUrl(String thread, String bbsToken) {
        String url = "https://www.hifini.com/" + thread;
        HttpRequest request = HttpRequest.get(url)
                .header("Cookie", "bbs_token=" + bbsToken);
        String listContent = request.execute().body();

        List<String> titles = ReUtil.findAll("music: \\[(.*?)\\]", listContent, 1);
        return Comm.parsePlayUrl(titles.get(0));
    }


    public static void main(String[] args) {
//        String t = "thread-106.htm";
//        String token = "B913yznE7cUXNjnWjIAJtIAHZdodup7D8iEnJmoP_2F3FUpn0h7FuSidh_2FYVUIUZ4bfjy3TSOnI0xstnhjTbfG9VJN56M_3D";
//        System.out.println(queryDownloadUrl(t, token));
        String u = "http://m704.music.126.net/20250430212951/62f2decc5eb3740447bbf8ee0955be32/jdymusic/obj/wo3DlMOGwrbDjj7DisKw/32283600345/f29b/d599/f294/a7361dca69f85d80e3c027f2ec0550cd.mp3?vuutv=JyXveJ3j82TJO7epUCbSnYtLuUCGJzaTtFazCRio2335tJubSgZ3sU4fHtcR2xlbO5MpX1ejiHidB973dZ3I3KTSx9Kwsf3xgg+SczxkEF0=&authSecret=0000019686ccd05c10e90a649a050006";
    }
}
