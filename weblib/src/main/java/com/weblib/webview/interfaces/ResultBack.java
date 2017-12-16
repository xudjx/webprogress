package com.weblib.webview.interfaces;

/**
 * Created by xud on 2017/8/16.
 *
 * 有一个原则，为了减少中间调用时重复封装的次数， Map result 中带上回调给webview的信息
 * key: callbackname,  value: 从传递的参数params中通过 getKey("callback") 方式获取
 */

public interface ResultBack {

    void onResult(int status, String action, Object result);
}
