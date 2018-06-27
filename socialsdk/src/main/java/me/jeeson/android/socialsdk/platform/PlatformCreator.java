package me.jeeson.android.socialsdk.platform;

import android.content.Context;

/**
 * 社会化平台的创建接口协议
 * Created by Jeeson on 2018/6/20.
 */
public interface PlatformCreator {
    IPlatform create(Context context, int target);
}
