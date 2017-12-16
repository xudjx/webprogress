package com.weblib.webview.view;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewParent;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.weblib.webview.R;
import com.weblib.webview.interfaces.JsRemoteInterface;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by xud on 2017/8/1.
 */
public class DWebView extends WebView {
    private static final ExecutorService THREAD_POOL = Executors.newSingleThreadExecutor();
    public static final String CONTENT_SCHEME = "file:///android_asset/";

    private ActionMode.Callback mCustomCallback;
    protected Context context;
    boolean isReady;

    private PageFinishedCallBack pageFinishedCallBack;
    private PageStartedCallBack pageStartedCallBack;
    private OverrideUrlLoadingCallBack urlLoadingCallBack;
    private Map<String, String> mHeaders;

    private OnErrorListener mOnErrorListener;

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

    public void setOnErrorListener(OnErrorListener onErrorListener) {
        mOnErrorListener = onErrorListener;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DWebView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    public void setPageFinishedCallBack(PageFinishedCallBack pageFinishedCallBack) {
        this.pageFinishedCallBack = pageFinishedCallBack;
    }

    public void setPageStartedCallBack(PageStartedCallBack pageStartedCallBack) {
        this.pageStartedCallBack = pageStartedCallBack;
    }

    public void setUrlLoadingCallBack(OverrideUrlLoadingCallBack urlLoadingCallBack) {
        this.urlLoadingCallBack = urlLoadingCallBack;
    }

    public void setHeaders(Map<String, String> mHeaders) {
        this.mHeaders = mHeaders;
    }

    protected void init(Context context) {
        this.context = context;

        //解决 在点击后自动滑到顶部的bug
//        setFocusable(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            enableSlowWholeDocumentDraw();
        }

        final WebSettings settings = getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setAppCacheEnabled(true);
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setDomStorageEnabled(true);
        //设置支持app cache
        String appCacheDir = getContext().getDir("cache", Context.MODE_PRIVATE).getPath();
        settings.setAppCachePath(appCacheDir);
        settings.setAllowFileAccess(true);
        settings.setAppCacheMaxSize(1024*1024*8);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);


        /**
         * Used with {@link android.webkit.WebSettings#MIXED_CONTENT_NEVER_ALLOW}
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            settings.setAllowUniversalAccessFromFileURLs(true);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        setWebViewClient(new DWebViewClient());

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

    public void release() {
        this.destroy();
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

    public interface PageFinishedCallBack {
        void pageFinished(String url);
    }

    public interface PageStartedCallBack {
        void pageStarted(String url);
    }

    public interface OverrideUrlLoadingCallBack {
        void overrideUrlLoading(WebView view, String url);
    }

    public class DWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
//            Log.e("jhy","override url:"+url);
            if (urlLoadingCallBack != null) {
                urlLoadingCallBack.overrideUrlLoading(view, url);
                return true;
            }
            view.loadUrl(url, mHeaders);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (!TextUtils.isEmpty(url) && url.startsWith(CONTENT_SCHEME)) {
                isReady = true;
            }
            if (pageFinishedCallBack != null) {
                pageFinishedCallBack.pageFinished(url);
            }
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            if (pageStartedCallBack != null) {
                pageStartedCallBack.pageStarted(url);
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
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            Log.e("webview error", errorCode + " + " + description);
            if(mOnErrorListener != null) {
                mOnErrorListener.onError();
            }
        }

        @Override
        public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
            String channel = "";
            ApplicationInfo appInfo = null;
            try {
                appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
                channel = appInfo.metaData.getString("TD_CHANNEL_ID");
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
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

    public boolean isReady() {
        return isReady;
    }

    public interface OnErrorListener {
        void onError();
    }

}