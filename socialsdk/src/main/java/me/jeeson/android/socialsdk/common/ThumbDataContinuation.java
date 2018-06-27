package me.jeeson.android.socialsdk.common;

import me.jeeson.android.socialsdk.exception.SocialError;
import me.jeeson.android.socialsdk.utils.SocialLogUtils;
import me.jeeson.android.socialsdk.listener.OnShareListener;

import bolts.Continuation;
import bolts.Task;

/**
 * 压缩图片之后的返回结果
 * Created by Jeeson on 2018/6/20.
 */
public abstract class ThumbDataContinuation implements Continuation<byte[], Object> {

    private String          tag;
    private String          msg;
    private OnShareListener onShareListener;

    protected ThumbDataContinuation(String tag, String msg, OnShareListener onShareListener) {
        this.tag = tag;
        this.msg = msg;
        this.onShareListener = onShareListener;
    }

    @Override
    public Object then(Task<byte[]> task) throws Exception {
        if (task.isFaulted() || task.getResult() == null) {
            SocialLogUtils.e(tag, "图片压缩失败 -> " + msg);
            onShareListener.onFailure(new SocialError(msg, task.getError()));
        } else {
            onSuccess(task.getResult());
        }
        return null;
    }

    public abstract void onSuccess(byte[] thumbData);
}