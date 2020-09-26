package com.milo.lyriclayout.util;

import com.milo.liblyric.MultiLyricData;

/**
 * Title：
 * Describe：
 * Remark：
 * <p>
 * Created by Milo
 * E-Mail : 303767416@qq.com
 * 2020/9/26
 */
public class LyricDataFactory {

    public static MultiLyricData createLyricData(String lyric, int beginTime){
        MultiLyricData lyricData = new MultiLyricData();
        lyricData.lyric = lyric;
        lyricData.beginTime = beginTime;
        lyricData.endTime = beginTime + lyric.length() * 300;
        return lyricData;
    }

}
