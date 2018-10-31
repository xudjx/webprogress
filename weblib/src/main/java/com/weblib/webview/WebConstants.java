package com.weblib.webview;

/**
 * Created by xud on 2017/8/16.
 */

public class WebConstants {

    public static final int LEVEL_UI = 0; // UI Command
    public static final int LEVEL_BASE = 1; // 基础level
    public static final int LEVEL_ACCOUNT = 2; // 涉及到账号相关的level

    public static final int CONTINUE = 2; // 继续分发command
    public static final int SUCCESS = 1; // 成功
    public static final int FAILED = 0; // 失败
    public static final String EMPTY = ""; // 无返回结果

    public static final String WEB2NATIVE_CALLBACk = "callback";
    public static final String NATIVE2WEB_CALLBACK = "callbackname";

    public static final String ACTION_EVENT_BUS = "eventBus";

    public static final String INTENT_TAG_TITLE = "title";
    public static final String INTENT_TAG_URL = "url";

    public static class ERRORCODE {
        public static final int NO_METHOD = -1000;
        public static final int NO_AUTH = -1001;
        public static final int NO_LOGIN = -1002;
        public static final int ERROR_PARAM = -1003;
        public static final int ERROR_EXCEPTION = -1004;
    }

    public static class ERRORMESSAGE {
        public static final String NO_METHOD = "方法找不到";
        public static final String NO_AUTH = "方法权限不够";
        public static final String NO_LOGIN = "尚未登录";
        public static final String ERROR_PARAM = "参数错误";
        public static final String ERROR_EXCEPTION = "未知异常";
    }
}
