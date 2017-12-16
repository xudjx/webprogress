package com.weblib.webview;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.weblib.webview.interfaces.Command;
import com.weblib.webview.interfaces.ResultBack;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xud on 2017/12/16.
 */

public class BaseLevelCommands {

    private HashMap<String, Command> commands;
    private Context mContext;

    public BaseLevelCommands(Context context) {
        this.mContext = context;
        registerCommands();
    }

    private void registerCommands() {
        commands = new HashMap<>();
        registerCommand(toastCommand);
    }

    private void registerCommand(Command command) {
        commands.put(command.name(), command);
    }

    public HashMap<String, Command> getCommands() {
        return commands;
    }

    private final Command toastCommand = new Command() {
        @Override
        public String name() {
            return "showToast";
        }

        @Override
        public void exec(final Context context, final Map params, ResultBack resultBack) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context.getApplicationContext(), String.valueOf(params.get("message")), Toast.LENGTH_SHORT).show();
                }
            });
        }
    };
}
