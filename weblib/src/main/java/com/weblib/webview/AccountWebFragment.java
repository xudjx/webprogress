package com.weblib.webview;

import android.os.Bundle;

/**
 * Created by xud on 2017/12/16.
 */

public class AccountWebFragment extends BaseWebviewFragment {

    public static AccountWebFragment newInstance(String keyUrl) {
        AccountWebFragment fragment = new AccountWebFragment();
        fragment.setArguments(getBundle(keyUrl));
        return fragment;
    }

    public static Bundle getBundle(String url) {
        Bundle bundle = new Bundle();
        bundle.putString(WebConstants.INTENT_TAG_URL, url);
        return bundle;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_common_webview;
    }

    @Override
    public int getCommandLevel() {
        return WebConstants.LEVEL_ACCOUNT;
    }
}
