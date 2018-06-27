# SocialSDK
对第三方社会化原生SDK集成和二次封装，提供微博、微信、QQ的登陆分享功能。
> 第三方社会化SDK均更新到最新的版本。
![截图](screenshot/screenshot-homepage.png "截图")

## 1 集成使用

> 在项目级别的 `build-gradle` 中添加

```groovy
allprojects {
    repositories {
        maven { url "https://dl.bintray.com/thelasterstar/maven" }      //微博sdk maven库
    }
}
```

> 导入module在app级别的 `build-gradle` 中添加

```groovy
dependencies {
    compile project(':socialsdk')
}
```

> app级别的 `build-gradle` 中`defaultConfig`配置申请的QQ的APP_ID

```groovy
defaultConfig {
    manifestPlaceholders.qq_id = "QQ的APP_ID"
}
```

## 主要类文件

> 使用 **SocialSDK** 只需要关注以下几个文件：

- `SocialSDK.java` 结合 `SocialSDKConfig.java` 用来进行授权信息的配置。

- `Target.java` 类是单独分离出来的常量类，指向了登录和分享的具体目标。

- `SocialLoginManager.java` 用来实现 QQ、微信、微博第三方授权登录，只需要调用 `LoginManager.login()` 方法，即可进行第三方授权登录。

- `SocialShareManager.java` 用来实现QQ、微信、微博渠道的分享，只要调用 `ShareManager.share()` 方法，即可进行第三方分享。
 
 ## 初始化

> 你需要在使用 SDK 之前进行初始化操作，建议放在 `Applicaton` 中进行。

```java
String qqAppId = "xxx";
String wxAppId = "xxx";
String wxSecretKey = "xxx";
String sinaAppId = "xxx";
String ddAppId = "xxx";
SocialSDKConfig config = new SocialSDKConfig(this)
        .dd(ddAppId)
        // 配置qq
        .qq(qqAppId)
        // 配置wx
        .wechat(wxAppId, wxSecretKey)
        // 配置sina
        .sina(sinaAppId)
        // 配置Sina的RedirectUrl，有默认值，如果是官网默认的不需要设置
        .sinaRedirectUrl("http://open.manfenmm.com/bbpp/app/weibo/common.php")
        // 配置Sina授权scope,有默认值，默认值 all
        .sinaScope(SocialConstants.SCOPE);
// 👮 添加 config 数据，必须
SocialSdk.init(config);
// 👮 添加自定义的 json 解析，必须
SocialSdk.setJsonAdapter(new GsonJsonAdapter());
// 这个不是必须的但是如果要使用微博的 openApi 需要重写该类，可以参考 temp 文件夹中的实现
SocialSdk.setJsonAdapter(new OkHttpRequestAdapter());
```

## Adapter的说明

> 使用 `adapter` 这种模式主要参照了一些成熟的类库，目的是为了对外提供更好的扩展性，这部分内容可以关注 `SocialSDK.java`。

- `IJsonAdapter`，负责 `Json` 解析，为了保持和宿主项目 `json` 解析框架的统一，是必须自定义添加的（没有内置一个实现是因为使用自带的 `JsonObject` 解析实在麻烦，又不想内置一个三方库进来，采取的这种折衷方案），提供一个 `Gson` 下的实现仅供参考 - [SocialSDKJsonAdapter.java](https://github.com/smuwjs/SocialSDK/blob/master/app/src/main/java/me/jeeson/android/demo/SocialSDKJsonAdapter.java)。

- `IRequestAdapter`，负责请求数据，目前微信的 `OAuth2` 授权和图片下载的相关请求都是使用 `IRequestAdapter` 代理，已经使用 `URLConnection` 内置了一个实现，如果你有自己的需求可以重写这部分，注意 `https` 请求的兼容，可以参考 - [RequestAdapterImpl.java](https://github.com/smuwjs/SocialSDK/blob/master/socialsdk/src/main/java/me/jeeson/android/socialsdk/adapter/impl/RequestAdapterImpl.java)。

## 登录和分享的使用

> 使用非常简单的，可以查看demo里面[MainActivity.java](https://github.com/smuwjs/SocialSDK/blob/master/app/src/main/java/me/jeeson/android/demo/SocialSDKJsonAdapter.java)的详细调用方式

## 引用

> 基于[SocialSdkLibrary](https://github.com/chendongMarch/SocialSdkLibrary)修改。