package com.zx.music.album;


import java.io.File;
import java.util.function.Function;

/**
 * @author xzhen
 * @creatAt 2026-02-28 17:04
 */
public interface AlbumProvider {

    File getAlbumImage(String musicId, String album, Function<String, File> consumer);
}
