package me.jeeson.android.socialsdk.model;

import android.content.Context;

import me.jeeson.android.socialsdk.R;
import me.jeeson.android.socialsdk.common.SocialConstants;

import java.io.File;

/**
 * 配置信息
 * Created by Jeeson on 2018/6/20.
 */
public class SocialSDKConfig {

    public static final String SHARE_CACHE_DIR_NAME = "toShare";

    // 调试配置
    private boolean isDebug = false;
    // 应用名
    private String appName = "应用";
    // 微信配置
    private String wxAppId;
    private String wxSecretKey;
    // qq 配置
    private String qqAppId;
    // 微博配置
    private String sinaAppId;
    // 配置Sina的RedirectUrl，有默认值（https://api.weibo.com/oauth2/default.html），如果是官网默认的不需要设置
    private String sinaRedirectUrl = SocialConstants.REDIRECT_URL;
    // 配置Sina授权scope,有默认值，默认值 all
    private String sinaScope       = SocialConstants.SCOPE;
    private String shareCacheDirPath;

    public SocialSDKConfig(Context context) {
        this.appName = context.getString(R.string.app_name);
        File shareDir = new File(context.getExternalCacheDir(), SHARE_CACHE_DIR_NAME);
        shareDir.mkdirs();
        shareCacheDirPath = shareDir.getAbsolutePath();
    }

    public String getShareCacheDirPath() {
        return shareCacheDirPath;
    }

    public SocialSDKConfig qq(String qqAppId) {
        this.qqAppId = qqAppId;
        return this;
    }

    public SocialSDKConfig wechat(String wxAppId, String wxSecretKey) {
        this.wxSecretKey = wxSecretKey;
        this.wxAppId = wxAppId;
        return this;
    }

    public SocialSDKConfig sina(String sinaAppId) {
        this.sinaAppId = sinaAppId;
        return this;
    }

    public SocialSDKConfig sinaRedirectUrl(String sinaRedirectUrl) {
        this.sinaRedirectUrl = sinaRedirectUrl;
        return this;
    }

    public SocialSDKConfig sinaScope(String sinaScope) {
        this.sinaScope = sinaScope;
        return this;
    }


    public String getAppName() {
        return appName;
    }

    public String getWxAppId() {
        return wxAppId;
    }

    public String getWxSecretKey() {
        return wxSecretKey;
    }

    public String getQqAppId() {
        return qqAppId;
    }

    public String getSinaAppId() {
        return sinaAppId;
    }

    public String getSinaRedirectUrl() {
        return sinaRedirectUrl;
    }

    public String getSinaScope() {
        return sinaScope;
    }

    public boolean isDebug() {
        return isDebug;
    }

    public SocialSDKConfig setDebug(boolean debug) {
        isDebug = debug;
        return this;
    }

    @Override
    public String toString() {
        return "SocialSDKConfig{" +
                "appName='" + appName + '\'' +
                ", wxAppId='" + wxAppId + '\'' +
                ", wxSecretKey='" + wxSecretKey + '\'' +
                ", qqAppId='" + qqAppId + '\'' +
                ", sinaAppId='" + sinaAppId + '\'' +
                ", sinaRedirectUrl='" + sinaRedirectUrl + '\'' +
                ", sinaScope='" + sinaScope + '\'' +
                ", shareCacheDirPath='" + shareCacheDirPath + '\'' +
                '}';
    }
}