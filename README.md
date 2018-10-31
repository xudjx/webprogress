# webprogress

详细说明请见文章：[Android WebView独立进程解决方案](http://www.jianshu.com/p/b66c225c19e2)

本项目主要包含两方面内容：

**WebView独立进程**

1. 定制DWebView，继承自WebView，满足了基本的WebView配置，用户可以直接使用；
2. 设计了几个WebFrament，用以页面加载
3. 设计了一套WebView独立进程和主进程交互的方案

**Web和Native交互解决方案**

1. 为方便Web和Native交互，提供了中间件dj.js;
2. 为方便Native处理Web请求，尤其是在WebView独立进程下的数据请求，设计了一套接口请求分发方案

**独特的Native Command注入和分发机制**

Command表示Native支持的Function，native将支持的Command提前注入到集合之中，细节之处可以查看源代码

测试所有的html存在assets目录下，测试的效果如下:

![](http://7xopuh.dl1.z0.glb.clouddn.com/WX20171216-203258@2x.png)
![](http://7xopuh.dl1.z0.glb.clouddn.com/WechatIMG68.jpeg)
