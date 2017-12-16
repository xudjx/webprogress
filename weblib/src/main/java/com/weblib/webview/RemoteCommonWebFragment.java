package com.weblib.webview;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

/**
 * Created by xud on 2017/8/10.
 */

public class RemoteCommonWebFragment extends RemoteBaseWebviewFragment {

    public String url;
    private final int COMMAND_LEVEL_BASE = CommandsManager.LEVEL_BASE;

    public static RemoteCommonWebFragment newInstance(String url) {
        RemoteCommonWebFragment fragment = new RemoteCommonWebFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("url", url);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if(bundle != null) {
            url = bundle.getString("url");
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            webView.clearCache(false);
        }
        bindMainWebHandleService();
    }

    @Override
    protected void initAfterServiceConnect() {
        webView.loadUrl(url);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_common_webview;
    }

    @Override
    protected void registerCommands() {

    }

    @Override
    protected int getCommandLevel() {
        return COMMAND_LEVEL_BASE;
    }

    @Override
    protected boolean preHandleActionCallback(int status, String actionName, String response) {
        return false;
    }
}
