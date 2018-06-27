package me.jeeson.android.socialsdk.listener.impl;

import me.jeeson.android.socialsdk.exception.SocialError;
import me.jeeson.android.socialsdk.listener.OnShareListener;
import me.jeeson.android.socialsdk.model.ShareObj;

/**
 * 简化版本分享监听
 *
 * Created by Jeeson on 2018/6/20.
 */
public class SimpleShareListener implements OnShareListener {

    @Override
    public void onStart(int shareTarget, ShareObj obj) {
    }

    @Override
    public ShareObj onPrepareInBackground(int shareTarget, ShareObj obj) throws Exception {
        return obj;
    }

    @Override
    public void onSuccess() {

    }

    @Override
    public void onFailure(SocialError e) {

    }

    @Override
    public void onCancel() {

    }
}
