package com.zx.music.lyric;

import cn.hutool.core.util.StrUtil;

import java.util.List;

/**
 * @author xzhen
 * @creatAt 2026-02-28 15:45
 */

public class LrcValidator {


    public static boolean isValidLrc(String lrcText) {
        if (StrUtil.isBlank(lrcText)) {
            return false;
        }

        try {
            List<String> lines = StrUtil.split(lrcText, "\n", true, true);

            return !LyricParser.parseLrc(lines).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
}
