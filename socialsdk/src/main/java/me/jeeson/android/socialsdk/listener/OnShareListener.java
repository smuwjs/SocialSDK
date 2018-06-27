package me.jeeson.android.socialsdk.listener;

import me.jeeson.android.socialsdk.exception.SocialError;
import me.jeeson.android.socialsdk.model.ShareObj;

/**
 * 分享监听回调
 * Created by Jeeson on 2018/6/20.
 */
public interface OnShareListener {

    void onStart(int shareTarget, ShareObj obj);

    /**
     * 准备工作，在子线程执行
     *
     * @param shareTarget 分享目标
     * @param obj         shareMediaObj
     */
    ShareObj onPrepareInBackground(int shareTarget, ShareObj obj) throws Exception;

    void onSuccess();

    void onFailure(SocialError e);

    void onCancel();
}
