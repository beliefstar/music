package com.zx.music.manager;

import com.zx.music.bean.MusicItem;
import com.zx.music.common.MusicExt;
import com.zx.music.manager.bean.MusicName;
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
    private String bbsToken = "uUqtAqYsveA0Pnn6hTP9W1yv3xfoZKkZ5zrLqEzE_2B_2BMZU5QJpwIvHXKRxsR_2B85sNVmZ0FF051lJSuhfV_2FbWf9M2SVfY_3D";

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
        MusicName musicName = MusicName.of(musicId);
        if (musicName == null || !MusicExt.contains(musicName.getExt())) {
            return null;
        }

        MusicItem mi = new MusicItem();
        mi.setName(musicName.decode());
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
    public boolean exists(String id) {
        for (MusicItem musicItem : musicItems) {
            if (musicItem.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }
}
