package me.jeeson.android.socialsdk.utils;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import me.jeeson.android.socialsdk.SocialSDK;
import me.jeeson.android.socialsdk.adapter.IJsonAdapter;
import me.jeeson.android.socialsdk.exception.SocialError;

import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;

/**
 * 使用外部注入的 json 转换类，减轻类库的依赖
 * Created by Jeeson on 2018/6/20.
 */
public class JsonUtils {

    public static final String TAG = JsonUtils.class.getSimpleName();

    public static <T> T getObject(String jsonString, Class<T> cls) {
        IJsonAdapter jsonAdapter = SocialSDK.getJsonAdapter();
        if (jsonAdapter != null) {
            try {
                return jsonAdapter.toObj(jsonString, cls);
            } catch (Exception e) {
                SocialLogUtils.e(TAG, e);
            }
        }
        return null;
    }

    public static String getObject2Json(Object object) {
        IJsonAdapter jsonAdapter = SocialSDK.getJsonAdapter();
        try {
            return jsonAdapter.toJson(object);
        } catch (Exception e) {
            SocialLogUtils.e(TAG, e);
        }
        return null;
    }

    public interface Callback<T> {

        void onSuccess(@NonNull T object);

        void onFailure(SocialError e);
    }

    public static <T> void startJsonRequest(final String url, final Class<T> clz, final Callback<T> callback) {
        SocialLogUtils.e("开始请求" + url);
        Task.callInBackground(new Callable<T>() {
            @Override
            public T call() throws Exception {
                T object = null;
                String json = SocialSDK.getRequestAdapter().getJson(url);
                if (!TextUtils.isEmpty(json)) {
                    SocialLogUtils.e("请求结果" + json);
                    object = getObject(json, clz);
                }
                return object;
            }
        }).continueWith(new Continuation<T, Boolean>() {
            @Override
            public Boolean then(Task<T> task) throws Exception {
                if (!task.isFaulted() && task.getResult() != null) {
                    callback.onSuccess(task.getResult());
                } else if (task.isFaulted()) {
                    callback.onFailure(new SocialError("startJsonRequest error", task.getError()));
                } else if (task.getResult() == null) {
                    callback.onFailure(new SocialError("json 无法解析"));
                } else {
                    callback.onFailure(new SocialError("unKnow error"));
                }
                return true;
            }
        }, Task.UI_THREAD_EXECUTOR).continueWith(new Continuation<Boolean, Object>() {
            @Override
            public Object then(Task<Boolean> task) throws Exception {
                if(task.isFaulted()) {
                    callback.onFailure(new SocialError("未 handle 的错误"));
                }
                return null;
            }
        });
    }
}
