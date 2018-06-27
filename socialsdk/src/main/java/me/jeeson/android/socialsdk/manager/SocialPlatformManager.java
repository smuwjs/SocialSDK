package me.jeeson.android.socialsdk.manager;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;

import me.jeeson.android.socialsdk.SocialSDK;
import me.jeeson.android.socialsdk.platform.IPlatform;
import me.jeeson.android.socialsdk.platform.Target;

/**
 * 静态持有 platform, 在流程结束后会回收所有资源
 * Created by Jeeson on 2018/6/20.
 */
public class SocialPlatformManager {

    public static final int INVALID_PARAM = -1;

    public static final int ACTION_TYPE_LOGIN = 0;
    public static final int ACTION_TYPE_SHARE = 1;

    public static final String KEY_SHARE_MEDIA_OBJ = "KEY_SHARE_MEDIA_OBJ"; // media obj key
    public static final String KEY_ACTION_TYPE = "KEY_ACTION_TYPE"; // action type

    public static final String KEY_SHARE_TARGET = "KEY_SHARE_TARGET"; // share target
    public static final String KEY_LOGIN_TARGET = "KEY_LOGIN_TARGET"; // login target

    private static IPlatform sIPlatform;

    static @NonNull
    IPlatform makePlatform(Context context, int target) {
        if (SocialSDK.getConfig() == null) {
            throw new IllegalArgumentException(Target.toDesc(target) + " SocialSDK.init() request");
        }
        IPlatform platform = SocialSDK.getPlatform(context, target);
        if (platform == null) {
            throw new IllegalArgumentException(Target.toDesc(target) + "  创建platform失败，请检查参数 " + SocialSDK.getConfig().toString());
        }
        sIPlatform =  platform;
        return platform;
    }

    public static IPlatform getPlatform() {
        return sIPlatform;
    }

    public static void release(Activity activity) {
        if (sIPlatform != null ) {
            sIPlatform.recycle();
            sIPlatform = null;
        }
        if (activity != null && !activity.isFinishing()) {
            activity.finish();
        }
    }

    public static void action(Activity activity, int actionType) {
        if (actionType != -1) {
            switch (actionType) {
                case SocialPlatformManager.ACTION_TYPE_LOGIN:
                    SocialLoginManager._actionLogin(activity);
                    break;
                case SocialPlatformManager.ACTION_TYPE_SHARE:
                    SocialShareManager._actionShare(activity);
                    break;
            }
        }
    }
}
