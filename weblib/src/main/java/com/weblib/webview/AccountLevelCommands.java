package com.weblib.webview;

import android.content.Context;
import android.text.TextUtils;

import com.weblib.webview.interfaces.AidlError;
import com.weblib.webview.interfaces.Command;
import com.weblib.webview.interfaces.ResultBack;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xud on 2017/12/16.
 */

public class AccountLevelCommands {

    private HashMap<String, Command> commands;
    private Context context;

    public AccountLevelCommands(Context context) {
        this.context = context;
        registerCommands();
    }

    private void registerCommands() {
        commands = new HashMap<>();
        registerCommand(appDataProviderCommand);
    }

    private void registerCommand(Command command) {
        commands.put(command.name(), command);
    }

    public HashMap<String, Command> getCommands() {
        return commands;
    }


    private final Command appDataProviderCommand = new Command() {
        @Override
        public String name() {
            return "appDataProvider";
        }

        @Override
        public void exec(Context context, Map params, ResultBack resultBack) {
            try {
                String callbackName = "";
                if (params.get("type") == null) {
                    AidlError aidlError = new AidlError(RemoteActionConstants.ERRORCODE.ERROR_PARAM, RemoteActionConstants.ERRORMESSAGE.ERROR_PARAM);
                    resultBack.onResult(RemoteActionConstants.FAILED, this.name(), aidlError);
                    return;
                }
                if (params.get(RemoteActionConstants.WEB2NATIVE_CALLBACk) != null) {
                    callbackName = params.get(RemoteActionConstants.WEB2NATIVE_CALLBACk).toString();
                }
                String type = params.get("type").toString();
                HashMap map = new HashMap();
                switch (type) {
                    case "account":
                        map.put("accountId", "test123456");
                        map.put("accountName", "xud");
                        break;
                }
                if (!TextUtils.isEmpty(callbackName)) {
                    map.put(RemoteActionConstants.NATIVE2WEB_CALLBACK, callbackName);
                }
                resultBack.onResult(RemoteActionConstants.SUCCESS, this.name(), map);
            } catch (Exception e) {
                e.printStackTrace();
                AidlError aidlError = new AidlError(RemoteActionConstants.ERRORCODE.ERROR_PARAM, RemoteActionConstants.ERRORMESSAGE.ERROR_PARAM);
                resultBack.onResult(RemoteActionConstants.FAILED, this.name(), aidlError);
            }
        }
    };
}
