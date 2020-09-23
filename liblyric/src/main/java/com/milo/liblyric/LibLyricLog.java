package com.milo.liblyric;

import android.util.Log;

/**
 * Title：日志
 * Describe：
 * Remark：
 * <p>
 * Created by Milo
 * E-Mail : 303767416@qq.com
 * 2020/9/23
 */
public class LibLyricLog {

    public static boolean logEnable = true;

    public static void d(String tag, String log){
        if(logEnable)
            Log.d(tag, log);
    }

}
