package me.jeeson.android.socialsdk.adapter;

import java.io.File;

/**
 * 请求的 adapter，可改成自己的网络请求
 *
 * Created by Jeeson on 2018/6/20.
 */
public interface IRequestAdapter {

    File getFile(String url);

    String getJson(String url);
}
