package com.weblib.webview;

import android.content.Context;

import com.weblib.webview.interfaces.AidlError;
import com.weblib.webview.interfaces.ResultBack;

import java.util.Map;

/**
 * Created by xud on 2017/8/16.
 */

public class CommandsManager {

    private static CommandsManager instance;

    public static final int LEVEL_BASE = 1; // 基础level
    public static final int LEVEL_ACCOUNT = 2; // 涉及到账号相关的level

    private Context context;
    private BaseLevelCommands baseLevelCommands;
    private AccountLevelCommands accountLevelCommands;

    private CommandsManager(Context context) {
        this.context = context;
        baseLevelCommands = new BaseLevelCommands(context);
        accountLevelCommands = new AccountLevelCommands(context);
    }

    public static CommandsManager getInstance(Context context) {
        if (instance == null) {
            synchronized (CommandsManager.class) {
                instance = new CommandsManager(context);
            }
        }
        return instance;
    }

    public void findAndExec(int level, String action, Map params, ResultBack resultBack) {
        boolean methodFlag = false;
        switch (level) {
            case LEVEL_BASE: {
                if (baseLevelCommands.getCommands().get(action) != null) {
                    methodFlag = true;
                    baseLevelCommands.getCommands().get(action).exec(context, params, resultBack);
                }
                if (accountLevelCommands.getCommands().get(action) != null) {
                    AidlError aidlError = new AidlError(RemoteActionConstants.ERRORCODE.NO_AUTH, RemoteActionConstants.ERRORMESSAGE.NO_AUTH);
                    resultBack.onResult(RemoteActionConstants.FAILED, action, aidlError);
                }
                break;
            }
            case LEVEL_ACCOUNT: {
                if (baseLevelCommands.getCommands().get(action) != null) {
                    methodFlag = true;
                    baseLevelCommands.getCommands().get(action).exec(context, params, resultBack);
                }
                if (accountLevelCommands.getCommands().get(action) != null) {
                    methodFlag = true;
                    accountLevelCommands.getCommands().get(action).exec(context, params, resultBack);
                }
                break;
            }
        }
        if (!methodFlag) {
            AidlError aidlError = new AidlError(RemoteActionConstants.ERRORCODE.NO_METHOD, RemoteActionConstants.ERRORMESSAGE.NO_METHOD);
            resultBack.onResult(RemoteActionConstants.FAILED, action, aidlError);
        }
    }

}
