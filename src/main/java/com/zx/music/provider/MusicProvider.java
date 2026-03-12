package com.zx.music.provider;


import com.zx.music.bean.SearchResult;

import java.util.List;

/**
 * @author xzhen
 * @creatAt 2026-03-12 10:09
 */
public interface MusicProvider {

    List<SearchResult> search(String keyword);

    String touch(String id);

}
