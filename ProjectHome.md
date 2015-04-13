# ![http://farm8.staticflickr.com/7426/9456847893_053161c7a4_o.png](http://farm8.staticflickr.com/7426/9456847893_053161c7a4_o.png)个人博客  [http://www.trinea.cn/](http://www.trinea.cn/) #
### 本项目已迁移至Github，此地址停止维护，最新内容请访问:[AndroidCommon@Github](https://github.com/trinea/AndroidCommon)，欢迎star或fork ###
<br />总结的一些android公共库，包含缓存(图片缓存、预取缓存)、公共View(下拉及底部加载更多ListView、底部加载更多ScrollView、滑动一页Gallery)、及工具类(下载管理、静默安装、shell工具类等等)。
具体使用可见[总结的一些android公共库](http://www.trinea.cn/android/android%E5%85%AC%E5%85%B1%E5%BA%93%E7%BC%93%E5%AD%98-%E4%B8%8B%E6%8B%89listview-%E4%B8%8B%E8%BD%BD%E7%AE%A1%E7%90%86pro-%E9%9D%99%E9%BB%98%E5%AE%89%E8%A3%85-root%E8%BF%90%E8%A1%8C-java%E5%85%AC/)。Demo APK地址见[trinea-android-demo.apk](https://play.google.com/store/apps/details?id=cn.trinea.android.demo)，主要包括：
## 一. 缓存类 ##
主要特性：(1).使用简单 (2). 轻松获取及预取取新图片(3).可选择多种缓存算法(FIFO、LIFO、LRU、MRU、LFU、MFU等13种)或自定义缓存算法 (4).省流量性能佳(有且仅有一个线程获取图片) (5).支持不同类型网络处理(6).可根据系统配置初始化缓存(7).扩展性强 (8).支持队列(9). 缓存可序列化到本地缓存 可从文件中恢复(10)包含map的大多数接口。
### 1. 图片内存缓存 ###
使用见：[图片内存缓存的使用](http://www.trinea.cn/android/android-imagecache%e5%9b%be%e7%89%87%e7%bc%93%e5%ad%98%ef%bc%8c%e4%bd%bf%e7%94%a8%e7%ae%80%e5%8d%95%ef%bc%8c%e6%94%af%e6%8c%81%e9%a2%84%e5%8f%96%ef%bc%8c%e6%94%af%e6%8c%81%e5%a4%9a%e7%a7%8d%e7%bc%93/)<br /> 适用：应用中获取图片较多且图片不大的应用，在微博、花瓣、美丽说、path这类应用中可以起到很好的效果。效果图如下：![http://farm4.staticflickr.com/3710/9312163125_81f1c1997b_o.jpg](http://farm4.staticflickr.com/3710/9312163125_81f1c1997b_o.jpg)

### 2. 图片SD卡缓存 ###
使用见：[图片SD卡缓存的使用](http://trinea.iteye.com/blog/1595423)<br />适用：应用中获取图片较多且图片较大的情况，在微博、花瓣、美丽说、path这类应用中可以起到很好的效果。效果图如下：
![http://farm3.staticflickr.com/2834/9314949798_ea69bdb5e8_o.jpg](http://farm3.staticflickr.com/2834/9314949798_ea69bdb5e8_o.jpg)

### 3. 预取数据缓存 ###
使用见：[预取数据缓存](http://www.trinea.cn/android/preloaddatacache%e6%94%af%e6%8c%81%e9%a2%84%e5%8f%96%e7%9a%84%e6%95%b0%e6%8d%ae%e7%bc%93%e5%ad%98%ef%bc%8c%e4%bd%bf%e7%94%a8%e7%ae%80%e5%8d%95%ef%bc%8c%e6%94%af%e6%8c%81%e5%a4%9a%e7%a7%8d%e7%bc%93/)
缓存类关系图如下：其中HttpCache为后续计划的http缓存
![http://farm4.staticflickr.com/3818/9318670513_1576253fd5_o.jpg](http://farm4.staticflickr.com/3818/9318670513_1576253fd5_o.jpg)

## 二. 公用的view ##
### 1. 下拉刷新及滚动到底部加载更多的Listview([DropDownListView](https://code.google.com/p/trinea-android-common/source/browse/trunk/src/cn/trinea/android/common/view/DropDownListView.java)) ###
使用: [滚动到底部加载更多及下拉刷新listview的使用](http://www.trinea.cn/android/滚动到底部加载更多及下拉刷新listview的使用)<br />
实现原理: http://trinea.iteye.com/blog/1562281,效果图如下：
![http://farm8.staticflickr.com/7376/9312162951_74b597ebaa_o.jpg](http://farm8.staticflickr.com/7376/9312162951_74b597ebaa_o.jpg)

### 2. 滑动一页(一个Item)的Gallery([SlideOnePageGallery](https://code.google.com/p/trinea-android-common/source/browse/trunk/src/cn/trinea/android/common/view/SlideOnePageGallery.java)) ###
使用及实现原理: [Gallery滑动一页(一个Item)效果](http://www.trinea.cn/android/gallery%E6%BB%91%E5%8A%A8%E4%B8%80%E9%A1%B5%E4%B8%80%E4%B8%AAitem%E6%95%88%E6%9E%9C/),效果图如下:

![http://farm8.staticflickr.com/7330/9321381014_fb404e2430_o.jpg](http://farm8.staticflickr.com/7330/9321381014_fb404e2430_o.jpg)


### 3. 滑动到底部或顶部响应的ScrollView([BorderScrollView](https://code.google.com/p/trinea-android-common/source/browse/trunk/src/cn/trinea/android/common/view/BorderScrollView.java)) ###
使用: [滚动到底部或顶部响应的ScrollView使用](http://www.trinea.cn/android/%E6%BB%9A%E5%8A%A8%E5%88%B0%E5%BA%95%E9%83%A8%E6%88%96%E9%A1%B6%E9%83%A8%E5%93%8D%E5%BA%94%E7%9A%84scrollview%E4%BD%BF%E7%94%A8/)<br />
实现原理: [滑动到底部或顶部响应的ScrollView实现](http://www.trinea.cn/android/%E6%BB%91%E5%8A%A8%E5%88%B0%E5%BA%95%E9%83%A8%E6%88%96%E9%A1%B6%E9%83%A8%E5%93%8D%E5%BA%94%E7%9A%84scrollview/)。效果图如下：

![http://farm4.staticflickr.com/3669/9459686814_1a523ceeb6_o.jpg](http://farm4.staticflickr.com/3669/9459686814_1a523ceeb6_o.jpg)


## 三. 工具类 ##
### 1. Android系统下载管理DownloadManager使用 ###
使用示例：[Android系统下载管理DownloadManager功能介绍及使用示例](http://www.trinea.cn/android/android%E7%B3%BB%E7%BB%9F%E4%B8%8B%E8%BD%BD%E7%AE%A1%E7%90%86downloadmanager%E5%8A%9F%E8%83%BD%E4%BB%8B%E7%BB%8D%E5%8F%8A%E4%BD%BF%E7%94%A8%E7%A4%BA%E4%BE%8B/)<br />
功能扩展：[Android下载管理DownloadManager功能扩展和bug修改](http://www.trinea.cn/android/android%E4%B8%8B%E8%BD%BD%E7%AE%A1%E7%90%86downloadmanager%E5%8A%9F%E8%83%BD%E5%A2%9E%E5%BC%BA%E5%92%8Cbug%E4%BF%AE%E6%94%B9/), 效果图如下：
![http://www.trinea.cn/wp-content/uploads/2013/05/downloadDemo2.gif](http://www.trinea.cn/wp-content/uploads/2013/05/downloadDemo2.gif)

### 2. Android APK root权限静默安装 ###
使用示例：[Android APK root权限静默安装](http://www.trinea.cn/android/android%E5%B8%B8%E7%94%A8%E4%BB%A3%E7%A0%81%E4%B9%8Bapk-root%E6%9D%83%E9%99%90%E9%9D%99%E9%BB%98%E5%AE%89%E8%A3%85/)

### 3. Android root权限 ###
直接调用[ShellUtils.execCommand](https://github.com/Trinea/AndroidCommon/blob/master/src/cn/trinea/android/common/util/ShellUtils.java#LC43)方法

### 4. 图片工具类 ###
(1)Drawable、Bitmap、byte数组相互转换; (2)根据url获得InputStream、Drawable、Bitmap