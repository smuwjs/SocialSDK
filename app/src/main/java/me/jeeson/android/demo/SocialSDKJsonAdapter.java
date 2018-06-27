package me.jeeson.android.demo;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.util.Date;

import me.jeeson.android.socialsdk.adapter.IJsonAdapter;


/**
 * 社会化SDK处理适配器
 * Created by Jeeson on 2018/6/22.
 */

public class SocialSDKJsonAdapter implements IJsonAdapter {

    @Override
    public <T> T toObj(String jsonString, Class<T> cls) {
        T t = null;
        try {
            Gson gson = new Gson();
            t = gson.fromJson(jsonString, cls);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return t;
    }

    @Override
    public String toJson(Object object) {
        GsonBuilder gsonBuilder = new GsonBuilder()
                .registerTypeAdapter(Date.class, new JsonSerializer<Date>() {
                    @Override
                    public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
                        return new JsonPrimitive(src.getTime());
                    }
                }).setDateFormat(DateFormat.LONG);
        return gsonBuilder.create().toJson(object);
    }
}
