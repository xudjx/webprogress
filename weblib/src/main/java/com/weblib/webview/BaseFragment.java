package com.weblib.webview;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

/**
 * Created by xud on 2017/12/16.
 */

public class BaseFragment extends Fragment {

    protected Context mContext;

    public void setTitle(int titleId) {
        getActivity().setTitle(titleId);
    }

    public void setTitle(CharSequence title) {
        getActivity().setTitle(title);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mContext == null) {
            mContext = getContext();
        }
    }

    @Override
    public Context getContext() {
        return super.getContext() == null ? mContext : super.getContext();
    }

}
