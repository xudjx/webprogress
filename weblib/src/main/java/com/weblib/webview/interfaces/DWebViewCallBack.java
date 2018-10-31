package com.weblib.webview.interfaces;

import android.content.Context;
import android.webkit.WebView;

/**
 * DWebView回调统一处理
 * 所有涉及到WebView交互的都必须实现这个callback
 */
public interface DWebViewCallBack {

    int getCommandLevel();

    void pageStarted(String url);

    void pageFinished(String url);

    boolean overrideUrlLoading(WebView view, String url);

    void onError();

    void exec(Context context, int commandLevel, String cmd, String params, WebView webView);
}
