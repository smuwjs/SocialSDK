package me.jeeson.android.socialsdk;

import android.content.Context;
import android.util.SparseArray;

import me.jeeson.android.socialsdk.adapter.IJsonAdapter;
import me.jeeson.android.socialsdk.adapter.IRequestAdapter;
import me.jeeson.android.socialsdk.adapter.impl.RequestAdapterImpl;
import me.jeeson.android.socialsdk.model.SocialSDKConfig;
import me.jeeson.android.socialsdk.platform.IPlatform;
import me.jeeson.android.socialsdk.platform.PlatformCreator;
import me.jeeson.android.socialsdk.platform.Target;
import me.jeeson.android.socialsdk.platform.tencent.QQPlatform;
import me.jeeson.android.socialsdk.platform.wechat.WxPlatform;
import me.jeeson.android.socialsdk.platform.weibo.WbPlatform;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 社会化SDK入口
 * Created by Jeeson on 2018/6/20.
 */
public class SocialSDK {

    private static SocialSDKConfig sSocialSdkConfig;
    private static IJsonAdapter sJsonAdapter;
    private static IRequestAdapter sRequestAdapter;
    private static SparseArray<PlatformCreator> sPlatformCreatorMap;
    private static ExecutorService sExecutorService;

    public static SocialSDKConfig getConfig() {
        if (sSocialSdkConfig == null) {
            throw new IllegalStateException("invoke SocialSDK.init() first please");
        }
        return sSocialSdkConfig;
    }

    public static void init(SocialSDKConfig config) {
        sSocialSdkConfig = config;
        sPlatformCreatorMap = new SparseArray<>();
        registerPlatform(new QQPlatform.Creator(), Target.LOGIN_QQ, Target.SHARE_QQ_FRIENDS, Target.SHARE_QQ_ZONE);
        registerPlatform(new WxPlatform.Creator(), Target.LOGIN_WX, Target.SHARE_WX_FAVORITE, Target.SHARE_WX_ZONE, Target.SHARE_WX_FRIENDS);
        registerPlatform(new WbPlatform.Creator(), Target.LOGIN_WB, Target.SHARE_WB_NORMAL, Target.SHARE_WB_OPENAPI);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Platform 注册
    ///////////////////////////////////////////////////////////////////////////

    private static void registerPlatform(PlatformCreator creator, int... targets) {
        for (int target : targets) {
            sPlatformCreatorMap.put(target, creator);
        }
    }

    public static IPlatform getPlatform(Context context, int target) {
        PlatformCreator creator = sPlatformCreatorMap.get(target);
        if (creator != null) {
            return creator.create(context, target);
        }
        return null;
    }

    ///////////////////////////////////////////////////////////////////////////
    // JsonAdapter
    ///////////////////////////////////////////////////////////////////////////

    public static IJsonAdapter getJsonAdapter() {
        if (sJsonAdapter == null) {
            throw new IllegalStateException("为了不引入其他的json解析依赖，特地将这部分放出去，必须添加一个对应的 json 解析工具，参考代码 sample/GsonJsonAdapter.java");
        }
        return sJsonAdapter;
    }

    public static void setJsonAdapter(IJsonAdapter jsonAdapter) {
        sJsonAdapter = jsonAdapter;
    }

    ///////////////////////////////////////////////////////////////////////////
    // RequestAdapter
    ///////////////////////////////////////////////////////////////////////////

    public static IRequestAdapter getRequestAdapter() {
        if (sRequestAdapter == null) {
            sRequestAdapter = new RequestAdapterImpl();
        }
        return sRequestAdapter;
    }

    public static void setRequestAdapter(IRequestAdapter requestAdapter) {
        sRequestAdapter = requestAdapter;
    }

    public static ExecutorService getExecutorService() {
        if (sExecutorService == null) {
            sExecutorService = Executors.newCachedThreadPool();
        }
        return sExecutorService;
    }
}
