package com.zx.music.album;


import cn.hutool.core.codec.Base64;
import lombok.Data;

import java.util.List;

/**
 * @author xzhen
 * @creatAt 2026-02-26 15:51
 */
@Data
public class AlbumData {

    private String album;

    private List<String> musics;

    public static void main(String[] args) {
        System.out.println(Base64.decodeStr("5byg5p2w44CK5Yu_5b-Y5b-D5a6J44CL"));
        System.out.println(Base64.decodeStr("5YmR5b-D"));
    }
}
