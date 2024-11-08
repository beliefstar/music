package com.zx.music.bean;

import lombok.Data;

import java.util.Date;

@Data
public class MusicItem {
    private String id;

    private String name;

    private Date addTime;
}
