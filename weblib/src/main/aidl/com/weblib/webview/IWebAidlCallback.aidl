// IWebAidlCallback.aidl
package com.weblib.webview;

// aidl调用后异步回调的接口

// responseCode 分为：成功 1， 失败 0. 失败时response返回{"code": 1, "message":"error message"}

interface IWebAidlCallback {
    void onResult(int responseCode, String actionName, String response);
}
