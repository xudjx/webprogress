package com.weblib.webview.command;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.widget.Toast;

import com.weblib.webview.WebConstants;
import com.weblib.webview.WebUtils;
import com.weblib.webview.interfaces.Command;
import com.weblib.webview.interfaces.ResultBack;

import java.util.List;
import java.util.Map;

/**
 * Created by xud on 2018/10/31
 */
public class UIDependencyCommands extends Commands {

    public UIDependencyCommands() {
        super();
        registCommands();
    }

    @Override
    int getCommandLevel() {
        return WebConstants.LEVEL_UI;
    }

    void registCommands() {
        registerCommand(showToastCommand);
        registerCommand(showDialogCommand);
    }

    /**
     * 显示Toast信息
     */
    private final Command showToastCommand = new Command() {
        @Override
        public String name() {
            return "showToast";
        }

        @Override
        public void exec(Context context, Map params, ResultBack resultBack) {
            Toast.makeText(context, String.valueOf(params.get("message")), Toast.LENGTH_SHORT).show();
        }
    };

    /**
     * 对话框显示
     */
    private final Command showDialogCommand = new Command() {
        @Override
        public String name() {
            return "showDialog";
        }

        @Override
        public void exec(Context context, Map params, final ResultBack resultBack) {
            if (WebUtils.isNotNull(params)) {
                String title = (String) params.get("title");
                String content = (String) params.get("content");
                int canceledOutside = 1;
                if (params.get("canceledOutside") != null) {
                    canceledOutside = (int) (double) params.get("canceledOutside");
                }
                List<Map<String, String>> buttons = (List<Map<String, String>>) params.get("buttons");
                final String callbackName = (String) params.get(WebConstants.WEB2NATIVE_CALLBACk);
                if (!TextUtils.isEmpty(content)) {
                    AlertDialog dialog = new AlertDialog.Builder(context)
                            .setTitle(title)
                            .setMessage(content)
                            .create();
                    dialog.setCanceledOnTouchOutside(canceledOutside == 1 ? true : false);
                    if (WebUtils.isNotNull(buttons)) {
                        for (int i = 0; i < buttons.size(); i++) {
                           final Map<String, String> button = buttons.get(i);
                            int buttonWhich = getDialogButtonWhich(i);

                            if (buttonWhich == 0) return;

                            dialog.setButton(buttonWhich, button.get("title"), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    button.put(WebConstants.NATIVE2WEB_CALLBACK, callbackName);
                                    resultBack.onResult(WebConstants.SUCCESS, name(), button);
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
