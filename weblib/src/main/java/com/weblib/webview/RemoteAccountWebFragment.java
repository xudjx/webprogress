package com.weblib.webview;

import android.os.Bundle;

/**
 * Created by xud on 2017/12/16.
 */

public class RemoteAccountWebFragment extends RemoteCommonWebFragment {

    private final int COMMAND_LEVEL_ACCOUNT = CommandsManager.LEVEL_ACCOUNT;

    public static RemoteAccountWebFragment newInstance(String url) {
        RemoteAccountWebFragment fragment = new RemoteAccountWebFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("url", url);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected int getCommandLevel() {
        return COMMAND_LEVEL_ACCOUNT;
    }
}
