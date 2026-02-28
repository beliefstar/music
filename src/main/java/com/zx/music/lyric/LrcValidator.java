package com.zx.music.lyric;

import cn.hutool.core.util.StrUtil;

import java.util.List;

/**
 * @author xzhen
 * @creatAt 2026-02-28 15:45
 */

public class LrcValidator {


    public static boolean isValidLrc(String lrcText) {
        if (lrcText == null || lrcText.trim().isEmpty()) {
            return false;
        }

        List<String> lines = StrUtil.split(lrcText, "\n", true, true);

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            // 1️⃣ 元信息行
            if (line.matches("^\\[(ti|ar|al|by|offset):.*\\]$")) {
                continue;
            }

            // 2️⃣ 段落标签行（如 [Verse] / [Chorus]）
            if (line.matches("^\\[[^:\\]]+\\]$")) {
                continue;
            }

            // 3️⃣ 时间戳歌词行
            if (line.matches("^(\\[(\\d{1,3}):(\\d{2})(?:\\.\\d{1,3})?\\])+.*$")) {

                // 校验秒是否合法
                var matcher = java.util.regex.Pattern
                        .compile("\\[(\\d{1,3}):(\\d{2})")
                        .matcher(line);

                while (matcher.find()) {
                    int sec = Integer.parseInt(matcher.group(2));
                    if (sec >= 60) {
                        return false;
                    }
                }

                continue;
            }

            // 其他情况一律非法
            return false;
        }

        return true;
    }
}
