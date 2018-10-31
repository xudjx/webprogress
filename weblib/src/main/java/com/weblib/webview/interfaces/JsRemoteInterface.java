package com.weblib.webview.interfaces;

import android.content.Context;
import android.os.Handler;
import android.webkit.JavascriptInterface;

/**
 * Created by xud on 2017/8/16.
 *
 * 1. 保留command的注册
 * 2. 不支持command通过远程aidl方式调用
 */

public final class JsRemoteInterface {

    private final Context mContext;
    private final Handler mHandler = new Handler();
    private AidlCommand aidlCommand;

    public JsRemoteInterface(Context context) {
        mContext = context;
    }

    @JavascriptInterface
    public void post(final String cmd, final String param) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (aidlCommand != null) {
                        aidlCommand.exec(mContext, cmd, param);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void setAidlCommand(AidlCommand aidlCommand) {
        this.aidlCommand = aidlCommand;
    }

    public interface AidlCommand {
        void exec(Context context, String cmd, String params);
    }
}
