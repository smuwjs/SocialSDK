package me.jeeson.android.socialsdk.listener;

import android.app.Activity;
import android.content.Intent;

/**
 * 平台周期方法
 * Created by Jeeson on 2018/6/20.
 */
public interface PlatformLifeCircle {

    void onActivityResult(int requestCode, int resultCode, Intent data);

    void handleIntent(Activity intent);

    void onResponse(Object resp);

    void recycle();
}