package com.zx.music.manager;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import com.zx.music.bean.MusicItem;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
@Component
public class MusicManager {

    private final CopyOnWriteArrayList<MusicItem> musicItems = new CopyOnWriteArrayList<>();

    @Setter
    private String bbsToken = "B913yznE7cUXNjnWjIAJtIAHZdodup7D8iEnJmoP_2F3FUpn0h7FuSidh_2FYVUIUZ4bfjy3TSOnI0xstnhjTbfG9VJN56M_3D";

    private synchronized void load() {
        File root = new File("store");
        if (!root.exists()) {
            root.mkdirs();
            return;
        }
        File[] files = root.listFiles();
        List<MusicItem> list = new ArrayList<>();
        for (File file : files) {
            String name = file.getName();
            MusicItem musicItem = parseMusicId(name, new Date(file.lastModified()));
            if (musicItem == null) {
                continue;
            }
            list.add(musicItem);
        }
        list.sort(((o1, o2) -> o2.getAddTime().compareTo(o1.getAddTime())));
        musicItems.addAll(list);
    }

    private MusicItem parseMusicId(String musicId, Date addTime) {
        if (!StrUtil.endWith(musicId, ".mp3")) {
            return null;
        }
        String base64Str = musicId.substring(0, musicId.length() - 4);
        String musicName = Base64.decodeStr(base64Str) + ".mp3";
        MusicItem mi = new MusicItem();
        mi.setName(musicName);
        mi.setAddTime(addTime);
        mi.setId(musicId);
        return mi;
    }

    @PostConstruct
    public void init() {
        load();
    }

    public void put(String id, Date addTime) {
        MusicItem musicItem = parseMusicId(id, addTime);
        musicItems.add(0, musicItem);
    }

    public void remove(String id) {
        musicItems.removeIf(musicItem -> musicItem.getId().equals(id));
    }

    public List<MusicItem> getMusicItems(String name) {
        List<MusicItem> list = new ArrayList<>();
        for (MusicItem musicItem : musicItems) {
            if (musicItem.getName().contains(name) || name.contains(musicItem.getName())) {
                list.add(musicItem);
            }
        }
        return list;
    }
}
