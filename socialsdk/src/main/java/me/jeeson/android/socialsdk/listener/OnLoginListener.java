package me.jeeson.android.socialsdk.listener;

import me.jeeson.android.socialsdk.exception.SocialError;
import me.jeeson.android.socialsdk.model.LoginResult;

/**
 * 登陆监听回调
 * Created by Jeeson on 2018/6/20.
 */
public interface OnLoginListener {

    void onStart();

    void onSuccess(LoginResult loginResult);

    void onCancel();

    void onFailure(SocialError e);
}
