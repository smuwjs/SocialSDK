package me.jeeson.android.socialsdk.utils;

import android.util.Log;

import me.jeeson.android.socialsdk.SocialSDK;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 日志工具类
 * Created by Jeeson on 2018/6/20.
 */
public class SocialLogUtils {

    public static final String TAG = "social-sdk";

    private static String getMsg(Object msg) {
        return msg == null ? "null" : msg.toString();
    }

    public static void e(String tag, Object msg) {
        if (SocialSDK.getConfig().isDebug())
            Log.e(tag + "|" + TAG, getMsg(msg));
    }

    public static void e(String tag, Object... msg) {
        if (SocialSDK.getConfig().isDebug()) {
            StringBuilder sb = new StringBuilder();
            for (Object o : msg) {
                sb.append(" ").append(getMsg(o)).append(" ");
            }
            Log.e(tag + "|" + TAG, sb.toString());
        }
    }

    public static void e(Object msg) {
        if (SocialSDK.getConfig().isDebug())
            Log.e(TAG, getMsg(msg));
    }

    public static void t(Throwable throwable) {
        Log.e(TAG, throwable.getMessage(), throwable);
    }


    public static void json(String tag, String json) {
        StringBuilder sb = new StringBuilder();
        if (json == null || json.trim().length() == 0) {
            sb.append("json isEmpty => ").append(json);
        } else {
            try {
                json = json.trim();
                if (json.startsWith("{")) {
                    JSONObject jsonObject = new JSONObject(json);
                    sb.append(jsonObject.toString(2));
                } else if (json.startsWith("[")) {
                    JSONArray jsonArray = new JSONArray(json);
                    sb.append(jsonArray.toString(2));
                } else {
                    sb.append("json 格式错误 => ").append(json);
                }
            } catch (Exception e) {
                e.printStackTrace();
                sb.append("json formatError => ").append(json);
            }
        }
        e(tag, sb.toString());
    }

}
