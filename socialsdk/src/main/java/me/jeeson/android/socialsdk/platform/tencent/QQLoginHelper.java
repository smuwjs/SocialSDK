package me.jeeson.android.socialsdk.platform.tencent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import me.jeeson.android.socialsdk.exception.SocialError;
import me.jeeson.android.socialsdk.model.AccessToken;
import me.jeeson.android.socialsdk.utils.JsonUtils;
import me.jeeson.android.socialsdk.utils.SocialLogUtils;
import me.jeeson.android.socialsdk.listener.OnLoginListener;
import me.jeeson.android.socialsdk.model.LoginResult;
import me.jeeson.android.socialsdk.platform.tencent.model.QQAccessToken;
import me.jeeson.android.socialsdk.platform.tencent.model.QQUser;
import me.jeeson.android.socialsdk.platform.Target;

import com.tencent.connect.UserInfo;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONObject;

import java.lang.ref.WeakReference;

/**
 * QQ登录辅助
 * 问题汇总：com.mTencentApi.tauth.AuthActivity需要添加（ <data android:scheme="tencentXXX" />）否则会一直返回分享取消
 * qq空间支持本地视频分享，网络视频使用web形式分享
 * qq好友不支持本地视频分享，支持网络视频分享
 *
 * Created by Jeeson on 2018/6/20.
 */
class QQLoginHelper {

    public static final String TAG = QQLoginHelper.class.getSimpleName();

    private int loginType;
    private Tencent mTencentApi;
    private WeakReference<Activity> mActivityRef;
    private OnLoginListener onLoginListener;
    private LoginUiListener loginUiListener;


    QQLoginHelper(Activity activity, Tencent mTencentApi, OnLoginListener onQQLoginListener) {
        this.mActivityRef = new WeakReference<>(activity);
        this.mTencentApi = mTencentApi;
        this.onLoginListener = onQQLoginListener;
        this.loginType = Target.LOGIN_QQ;
    }

    private Context getContext() {
        return mActivityRef.get().getApplicationContext();
    }

    public QQAccessToken getToken() {
        return AccessToken.getToken(getContext(), AccessToken.QQ_TOKEN_KEY, QQAccessToken.class);
    }

    // 接受登录结果
    void handleResultData(Intent data) {
        Tencent.handleResultData(data, this.loginUiListener);
    }

    // 登录
    public void login() {
        QQAccessToken qqToken = getToken();
        if (qqToken != null) {
            mTencentApi.setAccessToken(qqToken.getAccess_token(), qqToken.getExpires_in() + "");
            mTencentApi.setOpenId(qqToken.getOpenid());
            if (mTencentApi.isSessionValid()) {
                getUserInfo(qqToken);
            } else {
                loginUiListener = new LoginUiListener();
                mTencentApi.login(mActivityRef.get(), "all", loginUiListener);
            }
        } else {
            loginUiListener = new LoginUiListener();
            mTencentApi.login(mActivityRef.get(), "all", loginUiListener);
        }
    }

    // 登录监听包装类
    private class LoginUiListener implements IUiListener {
        @Override
        public void onComplete(Object o) {
            JSONObject jsonResponse = (JSONObject) o;
            QQAccessToken qqToken = JsonUtils.getObject(jsonResponse.toString(), QQAccessToken.class);
            SocialLogUtils.e(TAG, "获取到 qq token = ", qqToken);
            if (qqToken == null) {
                onLoginListener.onFailure(new SocialError("qq token is null, may be parse json error"));
                return;
            }
            // 保存token
            AccessToken.saveToken(getContext(), AccessToken.QQ_TOKEN_KEY, qqToken);
            mTencentApi.setAccessToken(qqToken.getAccess_token(), qqToken.getExpires_in() + "");
            mTencentApi.setOpenId(qqToken.getOpenid());
            getUserInfo(qqToken);
        }


        @Override
        public void onError(UiError e) {
            onLoginListener.onFailure(new SocialError("qq,获取用户信息失败 " + parseUiError(e)));
        }

        @Override
        public void onCancel() {
            onLoginListener.onCancel();
        }
    }

    // 获取用户信息
    private void getUserInfo(final QQAccessToken qqToken) {
        UserInfo info = new UserInfo(getContext(), mTencentApi.getQQToken());
        info.getUserInfo(new IUiListener() {
            @Override
            public void onComplete(Object object) {
                SocialLogUtils.e(TAG, "qq 获取到用户信息 = " + object);
                QQUser qqUserInfo = JsonUtils.getObject(object.toString(), QQUser.class);
                if (qqUserInfo == null) {
                    if (onLoginListener != null) {
                        onLoginListener.onFailure(new SocialError("解析 qq user 错误"));
                    }
                } else {
                    qqUserInfo.setOpenId(mTencentApi.getOpenId());
                    if (onLoginListener != null) {
                        onLoginListener.onSuccess(new LoginResult(loginType, qqUserInfo, qqToken));
                    }
                }
            }

            @Override
            public void onError(UiError e) {
                onLoginListener.onFailure(new SocialError("qq获取用户信息失败  " + parseUiError(e)));
            }

            @Override
            public void onCancel() {
                onLoginListener.onCancel();
            }

        });
    }

    public String parseUiError(UiError error) {
        return "qq error [ code = " + error.errorCode + ", msg = " + error.errorMessage + ", detail = " + error.errorDetail + " ]";
    }
}
