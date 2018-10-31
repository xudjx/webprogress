package com.weblib.webview.view;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.net.Uri;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.gson.Gson;
import com.weblib.webview.R;
import com.weblib.webview.interfaces.DWebViewCallBack;
import com.weblib.webview.interfaces.JsRemoteInterface;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by xud on 2018/9/1.
 */
public class DWebView extends WebView {

    private static final ExecutorService THREAD_POOL = Executors.newSingleThreadExecutor();

    private static final String TAG = "DWebView";

    public static final String CONTENT_SCHEME = "file:///android_asset/";

    private ActionMode.Callback mCustomCallback;

    protected Context context;

    boolean isReady;

    private DWebViewCallBack dWebViewCallBack;

    private Map<String, String> mHeaders;

    private JsRemoteInterface remoteInterface = null;

    public DWebView(Context context) {
        super(context);
        init(context);
    }

    public DWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DWebView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    public void registerdWebViewCallBack(DWebViewCallBack dWebViewCallBack) {
        this.dWebViewCallBack = dWebViewCallBack;
    }

    public void setHeaders(Map<String, String> mHeaders) {
        this.mHeaders = mHeaders;
    }

    protected void init(Context context) {
        this.context = context;
        WebDefaultSettingManager.getInstance().toSetting(this);
        setWebViewClient(new DWebViewClient());

        /**
         * Web Native交互触发
         */
        if (remoteInterface == null) {
            remoteInterface = new JsRemoteInterface(context);
            remoteInterface.setAidlCommand(new JsRemoteInterface.AidlCommand() {
                @Override
                public void exec(Context context, String cmd, String params) {
                    if (dWebViewCallBack != null) {
                        dWebViewCallBack.exec(context, dWebViewCallBack.getCommandLevel(), cmd, params, DWebView.this);
                    }
                }
            });
        }
        setJavascriptInterface(remoteInterface);
    }

    @Override
    public ActionMode startActionMode(ActionMode.Callback callback) {
        final ViewParent parent = getParent();
        if (parent != null) {
            return parent.startActionModeForChild(this, wrapCallback(callback));
        }
        return null;
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public ActionMode startActionMode(ActionMode.Callback callback, int type) {
        final ViewParent parent = getParent();
        if (parent != null) {
            return parent.startActionModeForChild(this, wrapCallback(callback), type);
        }
        return null;
    }

    private ActionMode.Callback wrapCallback(ActionMode.Callback callback) {
        if (mCustomCallback != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return new CallbackWrapperM(mCustomCallback, callback);
            } else {
                return new CallbackWrapperBase(mCustomCallback, callback);
            }
        }
        return callback;
    }

    public void setCustomActionCallback(ActionMode.Callback callback) {
        mCustomCallback = callback;
    }

    private static class CallbackWrapperBase implements ActionMode.Callback {
        private final ActionMode.Callback mWrappedCustomCallback;
        private final ActionMode.Callback mWrappedSystemCallback;

        public CallbackWrapperBase(ActionMode.Callback customCallback, ActionMode.Callback systemCallback) {
            mWrappedCustomCallback = customCallback;
            mWrappedSystemCallback = systemCallback;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            return mWrappedCustomCallback.onCreateActionMode(mode, menu);
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return mWrappedCustomCallback.onPrepareActionMode(mode, menu);
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return mWrappedCustomCallback.onActionItemClicked(mode, item);
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            try {
                mWrappedCustomCallback.onDestroyActionMode(mode);
                mWrappedSystemCallback.onDestroyActionMode(mode);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private static class CallbackWrapperM extends ActionMode.Callback2 {
        private final ActionMode.Callback mWrappedCustomCallback;
        private final ActionMode.Callback mWrappedSystemCallback;

        public CallbackWrapperM(ActionMode.Callback customCallback, ActionMode.Callback systemCallback) {
            mWrappedCustomCallback = customCallback;
            mWrappedSystemCallback = systemCallback;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            return mWrappedCustomCallback.onCreateActionMode(mode, menu);
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return mWrappedCustomCallback.onPrepareActionMode(mode, menu);
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return mWrappedCustomCallback.onActionItemClicked(mode, item);
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mWrappedCustomCallback.onDestroyActionMode(mode);
            mWrappedSystemCallback.onDestroyActionMode(mode);
        }

        @Override
        public void onGetContentRect(ActionMode mode, View view, Rect outRect) {
            if (mWrappedCustomCallback instanceof ActionMode.Callback2) {
                ((ActionMode.Callback2) mWrappedCustomCallback).onGetContentRect(mode, view, outRect);
            } else if (mWrappedSystemCallback instanceof ActionMode.Callback2) {
                ((ActionMode.Callback2) mWrappedSystemCallback).onGetContentRect(mode, view, outRect);
            } else {
                super.onGetContentRect(mode, view, outRect);
            }
        }
    }

    public void setContent(String htmlContent) {
        try {
            loadDataWithBaseURL(CONTENT_SCHEME, htmlContent, "text/html", "UTF-8", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint({"AddJavascriptInterface", "SetJavaScriptEnabled"})
    public void setJavascriptInterface(JsRemoteInterface obj) {
        addJavascriptInterface(obj, "webview");
    }

    public void exec(String trigger) {
        if (isReady) {
            load(trigger);
        } else {
            new WaitLoad(trigger).executeOnExecutor(THREAD_POOL);
        }
    }

    private void load(String trigger) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            evaluateJavascript(trigger, null);
        } else {
            loadUrl(trigger);
        }
    }

    private class WaitLoad extends AsyncTask<Void, Void, Void> {

        private String mTrigger;

        public WaitLoad(String trigger) {
            super();
            mTrigger = trigger;
        }

        @Override
        protected Void doInBackground(Void... params) {
            while (!DWebView.this.isReady) {
                sleep(100);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            load(mTrigger);
        }

        private synchronized void sleep(long ms) {
            try {
                wait(ms);
            } catch (InterruptedException ignore) {
            }
        }
    }

    @Override
    public void loadUrl(String url) {
        super.loadUrl(url);
        Log.e(TAG, "DWebView load url: " + url);
        resetAllStateInternal(url);
    }

    @Override
    public void loadUrl(String url, Map<String, String> additionalHttpHeaders) {
        super.loadUrl(url, additionalHttpHeaders);
        Log.e(TAG, "DWebView load url: " + url);
        resetAllStateInternal(url);
    }

    public void handleCallback(String response) {
        if (!TextUtils.isEmpty(response)) {
            String trigger = "javascript:" + "dj.callback" + "(" + response + ")";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                evaluateJavascript(trigger, null);
            } else {
                loadUrl(trigger);
            }
        }
    }

    public void loadJs(String trigger) {
        if (!TextUtils.isEmpty(trigger)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                evaluateJavascript(trigger, null);
            } else {
                loadUrl(trigger);
            }
        }
    }

    public void loadJS(String cmd, Object param) {
        String trigger = "javascript:" + cmd + "(" + new Gson().toJson(param) + ")";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            evaluateJavascript(trigger, null);
        } else {
            loadUrl(trigger);
        }
    }

    public void loadJS(String trigger) {
        if (!TextUtils.isEmpty(trigger)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                evaluateJavascript(trigger, null);
            } else {
                loadUrl(trigger);
            }
        }
    }

    public void dispatchEvent(String name) {
        Map<String, String> param = new HashMap<>(1);
        param.put("name", name);
        loadJS("dj.dispatchEvent", param);
    }

    public void dispatchEvent(Map params) {
        loadJS("dj.dispatchEvent", params);
    }

    @Override
    public void goBack() {
        super.goBack();
    }

    private boolean mTouchByUser;

    public boolean isTouchByUser() {
        return mTouchByUser;
    }

    private void resetAllStateInternal(String url) {
        if (!TextUtils.isEmpty(url) && url.startsWith("javascript:")) {
            return;
        }
        resetAllState();
    }

    // 加载url时重置touch状态
    protected void resetAllState() {
        mTouchByUser = false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchByUser = true;
                break;
        }
        return super.onTouchEvent(event);
    }

    public class DWebViewClient extends WebViewClient {

        public static final String SCHEME_SMS = "sms:";

        /**
         * url重定向会执行此方法以及点击页面某些链接也会执行此方法
         *
         * @return true:表示当前url已经加载完成，即使url还会重定向都不会再进行加载 false 表示此url默认由系统处理，该重定向还是重定向，直到加载完成
         */
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.e(TAG, "shouldOverrideUrlLoading url: " + url);
            // 当前链接的重定向, 通过是否发生过点击行为来判断
            if (!isTouchByUser()) {
                return super.shouldOverrideUrlLoading(view, url);
            }
            // 如果链接跟当前链接一样，表示刷新
            if (getUrl().equals(url)) {
                return super.shouldOverrideUrlLoading(view, url);
            }
            if (handleLinked(url)) {
                return true;
            }
            if (dWebViewCallBack != null && dWebViewCallBack.overrideUrlLoading(view, url)) {
                return true;
            }
            // 控制页面中点开新的链接在当前webView中打开
            view.loadUrl(url, mHeaders);
            return true;
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            Log.e(TAG, "shouldOverrideUrlLoading url: "+ request.getUrl());
            // 当前链接的重定向
            if (!isTouchByUser()) {
                return super.shouldOverrideUrlLoading(view, request);
            }
            // 如果链接跟当前链接一样，表示刷新
            if (getUrl().equals(request.getUrl().toString())) {
                return super.shouldOverrideUrlLoading(view, request);
            }
            if (handleLinked(request.getUrl().toString())) {
                return true;
            }
            if (dWebViewCallBack != null && dWebViewCallBack.overrideUrlLoading(view, request.getUrl().toString())) {
                return true;
            }
            // 控制页面中点开新的链接在当前webView中打开
            view.loadUrl(request.getUrl().toString(), mHeaders);
            return true;
        }

        /**
         * 支持电话、短信、邮件、地图跳转，跳转的都是手机系统自带的应用
         */
        private boolean handleLinked(String url) {
            if (url.startsWith(WebView.SCHEME_TEL)
                    || url.startsWith(SCHEME_SMS)
                    || url.startsWith(WebView.SCHEME_MAILTO)
                    || url.startsWith(WebView.SCHEME_GEO)) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    context.startActivity(intent);
                } catch (ActivityNotFoundException ignored) {
                    ignored.printStackTrace();
                }
                return true;
            }
            return false;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            Log.e(TAG, "onPageFinished url:" + url);
            if (!TextUtils.isEmpty(url) && url.startsWith(CONTENT_SCHEME)) {
                isReady = true;
            }
            if (dWebViewCallBack != null) {
                dWebViewCallBack.pageFinished(url);
            }
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.e(TAG, "onPageStarted url: " + url);
            if (dWebViewCallBack != null) {
                dWebViewCallBack.pageStarted(url);
            }
        }

        @Override
        public void onScaleChanged(WebView view, float oldScale, float newScale) {
            super.onScaleChanged(view, oldScale, newScale);
        }

        @TargetApi(21)
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            return shouldInterceptRequest(view, request.getUrl().toString());
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            return null;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            Log.e(TAG, "webview error" + errorCode + " + " + description);
            if (dWebViewCallBack != null) {
                dWebViewCallBack.onError();
            }
        }

        @Override
        public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
            String channel = "";
            if (!TextUtils.isEmpty(channel) && channel.equals("play.google.com")) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                String message = context.getString(R.string.ssl_error);
                switch (error.getPrimaryError()) {
                    case SslError.SSL_UNTRUSTED:
                        message = context.getString(R.string.ssl_error_not_trust);
                        break;
                    case SslError.SSL_EXPIRED:
                        message = context.getString(R.string.ssl_error_expired);
                        break;
                    case SslError.SSL_IDMISMATCH:
                        message = context.getString(R.string.ssl_error_mismatch);
                        break;
                    case SslError.SSL_NOTYETVALID:
                        message = context.getString(R.string.ssl_error_not_valid);
                        break;
                }
                message += context.getString(R.string.ssl_error_continue_open);

                builder.setTitle(R.string.ssl_error);
                builder.setMessage(message);
                builder.setPositiveButton(R.string.continue_open, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handler.proceed();
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handler.cancel();
                    }
                });
                final AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                handler.proceed();
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    public boolean isReady() {
        return isReady;
    }
}