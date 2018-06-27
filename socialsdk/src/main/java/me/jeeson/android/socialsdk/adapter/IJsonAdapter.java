package me.jeeson.android.socialsdk.adapter;

/**
 * json 转换适配接口协议
 *
 * Created by Jeeson on 2018/6/20.
 */
public interface IJsonAdapter {

    <T> T toObj(String jsonString, Class<T> cls);

    String toJson(Object object);
}
