package com.weblib.webview.interfaces;

import android.content.Context;

import java.util.Map;

/**
 * Created by xud on 2017/8/16.
 */

public interface Command {

    String name();

    void exec(Context context, Map params, ResultBack resultBack);
}
