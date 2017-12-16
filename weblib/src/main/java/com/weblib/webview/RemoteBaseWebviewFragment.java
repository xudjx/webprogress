package com.weblib.webview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;

import com.google.gson.Gson;
import com.weblib.webview.aidl.RemoteWebBinderPool;
import com.weblib.webview.interfaces.Command;
import com.weblib.webview.interfaces.JsRemoteInterface;
import com.weblib.webview.interfaces.ResultBack;
import com.weblib.webview.view.DWebView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xud on 2017/12/16.
 */

public abstract class RemoteBaseWebviewFragment extends BaseFragment {

    protected DWebView webView;

    protected JsRemoteInterface jsInterface;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        jsInterface = new JsRemoteInterface(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable
            Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutRes(), container, false);
        webView = view.findViewById(R.id.web_view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initWebView();
        registBaseCommands();
        if(savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unregistBaseCommands();
    }

    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface", "AddJavascriptInterface"})
    protected void initWebView() {
        final WebSettings settings = webView.getSettings();
        settings.setSaveFormData(true);
        webView.addJavascriptInterface(jsInterface, "webview");
    }

    private void registBaseCommands() {
        jsInterface.setAidlCommand(new JsRemoteInterface.AidlCommand() {
            @Override
            public void exec(String cmd, String params) {
                handleAction(cmd, params);
            }
        });
        registerCmd4JsInterface(showDialogCommand);
        registerCommands();
    }

    protected final void registerCmd4JsInterface(Command cmd) {
        jsInterface.registerCommand(cmd);
    }

    private void unregistBaseCommands() {
        jsInterface.unregisterAllCommands();
    }

    protected void handleAction(String action, String params) {
        try {
            webAidlInterface.handleWebAction(getCommandLevel(), action, params, new IWebAidlCallback.Stub() {
                @Override
                public void onResult(final int status, final String actionName, final String response) throws RemoteException {
                    Log.d("weblib", String.format("Callback result: action= %s, result= %s", actionName, response));
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!preHandleActionCallback(status, actionName, response)) {
                                Map params = new Gson().fromJson(response, Map.class);
                                if (!TextUtils.isEmpty(params.get(RemoteActionConstants.NATIVE2WEB_CALLBACK).toString())) {
                                    handleCallback(response);
                                }
                            }

                        }
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleCallback(String response) {
        if (!TextUtils.isEmpty(response)) {
            if (webView != null) {
                String trigger = "javascript:" + "dj.callback" + "(" + response + ")";
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    webView.evaluateJavascript(trigger, null);
                } else {
                    webView.loadUrl(trigger);
                }
            }
        }
    }

    public void loadJS(String cmd, Object param) {
        if (webView != null) {
            String trigger = "javascript:" + cmd + "(" + new Gson().toJson(param) + ")";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                webView.evaluateJavascript(trigger, null);
            } else {
                webView.loadUrl(trigger);
            }
        }
    }

    public void dispatchEvent(String name) {
        Map<String, String> param = new HashMap<>();
        param.put("name", name);
        loadJS("dj.dispatchEvent", param);
    }

    public void dispatchEvent(Map params) {
        loadJS("dj.dispatchEvent", params);
    }

    public void handleCallback(String functionName, HashMap hashMap) {
        if (!TextUtils.isEmpty(functionName)) {
            hashMap.put(RemoteActionConstants.NATIVE2WEB_CALLBACK, functionName);
            loadJS("dj.callback", hashMap);
        }
    }

    @LayoutRes
    protected abstract int getLayoutRes();

    protected abstract void registerCommands();

    protected abstract void initAfterServiceConnect();

    protected abstract int getCommandLevel();

    protected abstract boolean preHandleActionCallback(int status, String actionName, String response);

    protected IWebAidlInterface webAidlInterface;

    protected void bindMainWebHandleService() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                RemoteWebBinderPool binderPool = RemoteWebBinderPool.getInstance(getActivity());
                IBinder iBinder = binderPool.queryBinder(RemoteWebBinderPool.BINDER_WEB_AIDL);
                webAidlInterface = IWebAidlInterface.Stub.asInterface(iBinder);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initAfterServiceConnect();
                    }
                });
            }
        }).start();
    }


    /**
     * 对话框显示
     */
    private final Command showDialogCommand = new Command() {
        @Override
        public String name() {
            return "showDialog";
        }

        @Override
        public void exec(Context context, Map params, ResultBack resultBack) {
            if (params != null && params.size() > 0) {
                String title = (String) params.get("title");
                String content = (String) params.get("content");
                int canceledOutside = 1;
                if (params.get("canceledOutside") != null) {
                    canceledOutside = (int) (double) params.get("canceledOutside");
                }
                List<Map<String, String>> buttons = (List<Map<String, String>>) params.get("buttons");
                final String callbackName = (String) params.get(RemoteActionConstants.WEB2NATIVE_CALLBACk);

                if (!TextUtils.isEmpty(content)) {
                    AlertDialog dialog = new AlertDialog.Builder(getContext())
                            .setTitle(title)
                            .setMessage(content)
                            .create();
                    dialog.setCanceledOnTouchOutside(canceledOutside == 1 ? true : false);

                    if (buttons != null && buttons.size() > 0) {
                        for (int i = 0; i < buttons.size(); i++) {
                            final Map<String, String> button = buttons.get(i);
                            int buttonWhich = getDialogButtonWhich(i);

                            if (buttonWhich == 0) return;

                            dialog.setButton(buttonWhich, button.get("title"), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    button.put(RemoteActionConstants.NATIVE2WEB_CALLBACK, callbackName);
                                    loadJS("dj.callback", button);
                                }
                            });
                        }
                    }

                    dialog.show();
                }
            }
        }

        private int getDialogButtonWhich(int index) {
            switch (index) {
                case 0:
                    return DialogInterface.BUTTON_POSITIVE;
                case 1:
                    return DialogInterface.BUTTON_NEGATIVE;
                case 2:
                    return DialogInterface.BUTTON_NEUTRAL;
            }
            return 0;
        }
    };

}
