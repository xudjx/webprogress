var dj = {};
dj.os = {};
dj.os.isIOS = /iOS|iPhone|iPad|iPod/i.test(navigator.userAgent);
dj.os.isAndroid = !dj.os.isIOS;
dj.callbackname = function(){
    return "djapi_callback_" + (new Date()).getTime() + "_" + Math.floor(Math.random() * 10000);
};
dj.callbacks = {};
dj.addCallback = function(name,func,userdata){
    delete dj.callbacks[name];
    dj.callbacks[name] = {callback:func,userdata:userdata};
};

dj.callback = function(para){
    var callbackobject = dj.callbacks[para.callbackname];
    if (callbackobject !== undefined){
        if (callbackobject.userdata !== undefined){
            callbackobject.userdata.callbackData = para;
        }
        if(callbackobject.callback != undefined){
            var ret = callbackobject.callback(para,callbackobject.userdata);
            if(ret === false){
                return
            }
            delete dj.callbacks[para.callbackname];
        }
    }
};

dj.post = function(cmd,para){
    if(dj.os.isIOS){
        var message = {};
        message.meta = {
            cmd:cmd
        };
        message.para = para || {};
        window.webview.post(message);
    }else if(window.dj.os.isAndroid){
        window.webview.post(cmd,JSON.stringify(para));
    }
};
dj.postWithCallback = function(cmd,para,callback,ud){
    var callbackname = dj.callbackname();
    dj.addCallback(callbackname,callback,ud);
    if(dj.os.isIOS){
        var message = {};
        message.meta  = {
            cmd:cmd,
            callback:callbackname
        };
        message.para = para;
        window.webview.post(message);
    }else if(window.dj.os.isAndroid){
        para.callback = callbackname;
        window.webview.post(cmd,JSON.stringify(para));
    }
};
dj.dispatchEvent = function(para){
    if (!para) {
        para = {"name":"webviewLoadComplete"};
    }
    var evt = {};
    try {
        evt = new Event(para.name);
        evt.para = para.para;
    } catch(e) {
        evt = document.createEvent("HTMLEvents");
        evt.initEvent(para.name, false, false);
    }
    window.dispatchEvent(evt);
};
dj.addEventListener = window.addEventListener;

dj.testFun = function(){
    try{
        window.dj.post("verifySubmitSuccess",{});
    } catch (e) {
        console.log(e);
    }
};
dj.stringify = function(obj){
    var type = typeof obj;
    if (type == "object"){
        return JSON.stringify(obj);
    }else {
        return obj;
    }
};
dj.nativecallback = function(obj){
    if(dj.os.isIOS){
        return dj.stringify(obj.data);
    }else if(window.dj.os.isAndroid){
        window.webview.post(obj.callback,dj.stringify(obj));
    }
};

dj.http = function(envcmd,options){
    var para = {
        url:options.url,
        type:options.type || "get",
        timeout:options.timeout || 60000, // 60 second
        data:options.data,
        contentType:options.contentType,
        responseType:options.responseType,
        headers:options.headers
    }
    var ud = {
        envcmd:envcmd,
        para:para,
        callbacks:{
            success:options.success,
            complete:options.complete,
            beforeSend:options.beforeSend,
            error:options.error
        }
    }
    ud.callbacks.beforeSend ? ud.callbacks.beforeSend() : ""
    dj.postWithCallback(envcmd,para,function(para,ud){
        if (para.success){
            ud.callbacks.success ? ud.callbacks.success(para.data) : ""
        }else{
            ud.callbacks.error ? ud.callbacks.error(null,para.errorReason) : ""
        }
        ud.callbacks.complete ? ud.callbacks.complete() : ""
    },ud)
}

dj.djApi = function(options){
    return dj.http("djapi",options)
}
dj.studioApi = function(options){
    return dj.http("studioapi",options)
}
dj.parseQuery = function(query){
    var reg = /([^=&\s]+)[=\s]*([^=&\s]*)/g;
    var obj = {};
    while(reg.exec(query)){
        obj[RegExp.$1] = RegExp.$2;
    }
    return obj;
}
dj.parseUrl = function(url){
  var a = document.createElement('a')
  a.href = url
  var querys = dj.parseQuery((a.search || "").replace("?",""))
  return { protocol:a.protocol || "",host:a.host || "",querys:querys,path:a.pathname || "" }
}
dj.newPage = function(paras){
  var url = paras.url
  if (url == undefined || url == ""){
    return
  }
  var urlinfo = dj.parseUrl(url)
  if(urlinfo.protocol == "dajia:"){
    var openPageParas = {
      object_title:paras.title,
      object_type:urlinfo.querys.objectType,
      object_info:urlinfo.querys
    }
    var messageParas = {
      title:paras.title,
      content:"",
      action:"OPEN_APP_INTERNAL_PAGE",
      extra:openPageParas,
        needCloseSelf: paras.needCloseSelf,
      url:paras.url
    }
    dj.post("newPage",messageParas)
  }else{
    dj.post("newPage",paras)
  }
}


/**
 * 跳转至新的web页面
 */
dj.newWebPage = function(url, title, needCloseSelf) {
    needCloseSelf = !!needCloseSelf ? needCloseSelf : false;
    try {
        dj.newPage({
            title: title,
            url: url,
            needCloseSelf: needCloseSelf
        });
    } catch (e) {
        document.location = url;
    }
};


dj.toast = function(msg) {
  dj.post("showToast",{message:msg})
}
dj.alert = function(para) {
    var title = para.title || ""
    var content = para.content || ""
    dj.postWithCallback("showDialog",{title:title,content:content,buttons:[{title:"知道了"}]},function(paras){})
}
dj.getSelectionText = function(){
    return dj.nativecallback({callback:"select_text",data:document.getSelection().toString()});
};
dj.reportAnalysisEvent = function(id, args) {
    try {
        args = !!args ? args : {};
        dj.post("reportAnalysisEvent",{id: id, paras: args});
    } catch (e) {
        console.log("统计失败");
    }
};

window.dj = dj;
