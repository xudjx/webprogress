package com.weblib.webview.view;

import android.webkit.WebView;

/**
 * Created by xud on 2018/9/5
 */
public interface AgentWebSettings<T extends android.webkit.WebSettings>{

    AgentWebSettings toSetting(WebView webView);

    T getWebSettings();
}
