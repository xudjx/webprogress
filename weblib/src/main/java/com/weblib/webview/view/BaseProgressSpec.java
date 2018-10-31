package com.weblib.webview.view;

/**
 * Created by xud on 2018/1/22.
 */

public interface BaseProgressSpec {
    void show();

    void hide();

    void reset();

    void setProgress(int newProgress);
}
