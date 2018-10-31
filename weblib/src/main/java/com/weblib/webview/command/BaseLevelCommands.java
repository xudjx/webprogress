package com.weblib.webview.command;

import android.content.Context;

import com.weblib.webview.WebConstants;
import com.weblib.webview.interfaces.Command;
import com.weblib.webview.interfaces.ResultBack;

import java.util.Map;

/**
 * Created by xud on 2017/12/16.
 */

public class BaseLevelCommands extends Commands {

    public BaseLevelCommands() {
        registerCommands();
    }

    @Override
    int getCommandLevel() {
        return WebConstants.LEVEL_BASE;
    }

    void registerCommands() {
        registerCommand(pageRouterCommand);
    }

    /**
     * 页面路由
     */
    private final Command pageRouterCommand = new Command() {
        @Override
        public String name() {
            return "newPage";
        }

        @Override
        public void exec(Context context, Map params, ResultBack resultBack) {
            String newUrl = params.get("url").toString();
            String title = (String) params.get("title");
        }
    };
}
