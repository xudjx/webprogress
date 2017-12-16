package com.weblib.webview;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by xud on 2017/8/10.
 */

public class RemoteCommonWebActivity extends AppCompatActivity {

    private String title;
    private String url;

    public static void start(Context context, String title, String url) {
        Intent intent = new Intent(context, RemoteCommonWebActivity.class);
        intent.putExtra(RemoteActionConstants.INTENT_TAG_TITLE, title);
        intent.putExtra(RemoteActionConstants.INTENT_TAG_URL, url);
        if (context instanceof Service) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_web);
        title = getIntent().getStringExtra(RemoteActionConstants.INTENT_TAG_TITLE);
        url = getIntent().getStringExtra(RemoteActionConstants.INTENT_TAG_URL);

        setTitle(title);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.web_view_fragment, RemoteCommonWebFragment.newInstance(url)).commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
