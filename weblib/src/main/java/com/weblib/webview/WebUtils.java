package com.weblib.webview;

import android.content.Context;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by xud on 2018/10/31
 */
public class WebUtils {

    public static int dipToPx(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }


    public static boolean isNotNull(List list) {
        if (list != null && list.size() > 0) {
            return true;
        }
        return false;
    }

    public static boolean isNotNull(Set set) {
        if (set != null && set.size() > 0) {
            return true;
        }
        return false;
    }

    public static boolean isNotNull(Map map) {
        if (map != null && map.size() > 0) {
            return true;
        }
        return false;
    }

}
