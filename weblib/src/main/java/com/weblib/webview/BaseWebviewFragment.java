package com.weblib.webview;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.weblib.webview.interfaces.Action;
import com.weblib.webview.interfaces.DWebViewCallBack;
import com.weblib.webview.interfaces.DefaultWebLifeCycleImpl;
import com.weblib.webview.interfaces.WebLifeCycle;
import com.weblib.webview.view.DWebView;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xud on 2017/12/16.
 */

public abstract class BaseWebviewFragment extends BaseFragment implements DWebViewCallBack {

    protected WebLifeCycle webLifeCycle;

    protected DWebView webView;

    public String webUrl;

    @LayoutRes
    protected abstract int getLayoutRes();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            webUrl = bundle.getString(WebConstants.INTENT_TAG_URL);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutRes(), container, false);
        webView = view.findViewById(R.id.web_view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        webLifeCycle = new DefaultWebLifeCycleImpl(webView);
        webView.registerdWebViewCallBack(this);
        CommandDispatcher.getInstance().initAidlConnect(getContext(), new Action() {
            @Override
            public void call(Object o) {
                MainLooper.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadUrl();
                    }
                });
            }
        });
    }

    protected void loadUrl() {
        webView.loadUrl(webUrl);
    }

    @Override
    public void onResume() {
        super.onResume();
        webView.dispatchEvent("pageResume");
        webLifeCycle.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        webView.dispatchEvent("pagePause");
        webLifeCycle.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        webView.dispatchEvent("pageStop");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        webView.dispatchEvent("pageDestroy");
        webLifeCycle.onDestroy();
    }


    @Override
    public int getCommandLevel() {
        return WebConstants.LEVEL_BASE;
    }

    @Override
    public void pageStarted(String url) {

    }

    @Override
    public void pageFinished(String url) {

    }

    @Override
    public boolean overrideUrlLoading(WebView view, String url) {
        return false;
    }

    @Override
    public void onError() {

    }

    @Override
    public void exec(Context context, int commandLevel, String cmd, String params, WebView webView) {
        CommandDispatcher.getInstance().exec(context, commandLevel, cmd, params, webView, getDispatcherCallBack());
    }

    protected CommandDispatcher.DispatcherCallBack getDispatcherCallBack() {
        return null;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == event.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            return onBackHandle();
        }
        return false;
    }

    protected boolean onBackHandle() {
        if (webView != null) {
            if (webView.canGoBack()) {
                webView.goBack();
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

}
