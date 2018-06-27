package me.jeeson.android.socialsdk.manager;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import me.jeeson.android.socialsdk.exception.SocialError;
import me.jeeson.android.socialsdk.listener.OnLoginListener;
import me.jeeson.android.socialsdk.model.AccessToken;
import me.jeeson.android.socialsdk.model.LoginResult;
import me.jeeson.android.socialsdk.platform.IPlatform;
import me.jeeson.android.socialsdk.platform.Target;
import me.jeeson.android.socialsdk.uikit.ActionActivity;
import me.jeeson.android.socialsdk.utils.SocialLogUtils;


import java.lang.ref.WeakReference;

/**
 * 登陆管理类，使用该类进行登陆操作
 * Created by Jeeson on 2018/6/20.
 */
public class SocialLoginManager {

    public static final String TAG = SocialLoginManager.class.getSimpleName();

    static OnLoginListener sListener;

    /**
     * 开始登陆，供外面使用
     *
     * @param context       context
     * @param loginTarget   登陆类型
     * @param loginListener 登陆监听
     */
    @TargetApi(Build.VERSION_CODES.ECLAIR)
    public static void login(Context context, @Target.LoginTarget int loginTarget, OnLoginListener loginListener) {
        sListener = loginListener;
        if(loginListener != null) {
            loginListener.onStart();
        }
        IPlatform platform = SocialPlatformManager.makePlatform(context, loginTarget);
        if (!platform.isInstall(context)) {
            loginListener.onFailure(new SocialError(SocialError.CODE_NOT_INSTALL));
            return;
        }
        Intent intent = new Intent(context, ActionActivity.class);
        intent.putExtra(SocialPlatformManager.KEY_ACTION_TYPE, SocialPlatformManager.ACTION_TYPE_LOGIN);
        intent.putExtra(SocialPlatformManager.KEY_LOGIN_TARGET, loginTarget);
        context.startActivity(intent);
        if (context instanceof Activity) {
            ((Activity)context).overridePendingTransition(0, 0);
        }
    }

    /**
     * 判断是否安装
     *
     * @param context       context
     * @param loginTarget   登陆类型
     */
    @TargetApi(Build.VERSION_CODES.ECLAIR)
    public static boolean isInstall(Context context, @Target.LoginTarget int loginTarget) {
        IPlatform platform = SocialPlatformManager.makePlatform(context, loginTarget);
        return platform.isInstall(context);
    }


    /**
     * 激活登陆
     *
     * @param activity activity
     */
    static void _actionLogin(final Activity activity) {
        Intent intent = activity.getIntent();
        int actionType = intent.getIntExtra(SocialPlatformManager.KEY_ACTION_TYPE, SocialPlatformManager.INVALID_PARAM);
        int loginTarget = intent.getIntExtra(SocialPlatformManager.KEY_LOGIN_TARGET, SocialPlatformManager.INVALID_PARAM);
        if (actionType == SocialPlatformManager.INVALID_PARAM) {
            SocialLogUtils.e(TAG, "_actionLogin actionType无效");
            return;
        }
        if (actionType != SocialPlatformManager.ACTION_TYPE_LOGIN) {
            return;
        }
        if (loginTarget == SocialPlatformManager.INVALID_PARAM) {
            SocialLogUtils.e(TAG, "shareTargetType无效");
            return;
        }
        if (sListener == null) {
            SocialLogUtils.e(TAG, "请设置 OnLoginListener");
            return;
        }
        if (SocialPlatformManager.getPlatform() == null) {
            return;
        }
        SocialPlatformManager.getPlatform().login(activity, new FinishLoginListener(activity));
    }


    static class FinishLoginListener implements OnLoginListener {

        private WeakReference<Activity> mActivityWeakRef;

        FinishLoginListener(Activity activity) {
            mActivityWeakRef = new WeakReference<>(activity);
        }

        @Override
        public void onStart() {
            if (sListener != null) sListener.onStart();
        }

        private void finish() {
            SocialPlatformManager.release(mActivityWeakRef.get());
            sListener = null;
        }

        @Override
        public void onSuccess(LoginResult loginResult) {
            if (sListener != null) sListener.onSuccess(loginResult);
            finish();
        }

        @Override
        public void onCancel() {
            if (sListener != null) sListener.onCancel();
            finish();
        }

        @Override
        public void onFailure(SocialError e) {
            if (sListener != null) sListener.onFailure(e);
            finish();
        }
    }

    public static void clearAllToken(Context context) {
        clearToken(context, Target.LOGIN_QQ);
        clearToken(context, Target.LOGIN_WX);
        clearToken(context, Target.LOGIN_WB);
    }

    public static void clearToken(Context context, @Target.LoginTarget int loginTarget) {
        AccessToken.clearToken(context, loginTarget);
    }
}
