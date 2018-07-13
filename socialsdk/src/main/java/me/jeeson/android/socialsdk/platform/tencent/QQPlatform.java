package me.jeeson.android.socialsdk.platform.tencent;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import me.jeeson.android.socialsdk.SocialSDK;
import me.jeeson.android.socialsdk.common.SocialConstants;
import me.jeeson.android.socialsdk.exception.SocialError;
import me.jeeson.android.socialsdk.model.SocialSDKConfig;
import me.jeeson.android.socialsdk.platform.IPlatform;
import me.jeeson.android.socialsdk.platform.PlatformCreator;
import me.jeeson.android.socialsdk.utils.FileUtils;
import me.jeeson.android.socialsdk.utils.CommonUtils;
import me.jeeson.android.socialsdk.utils.SocialLogUtils;
import me.jeeson.android.socialsdk.listener.OnLoginListener;
import me.jeeson.android.socialsdk.listener.OnShareListener;
import me.jeeson.android.socialsdk.model.ShareObj;
import me.jeeson.android.socialsdk.platform.AbsPlatform;
import me.jeeson.android.socialsdk.platform.Target;
import com.tencent.connect.common.Constants;
import com.tencent.connect.share.QQShare;
import com.tencent.connect.share.QzonePublish;
import com.tencent.connect.share.QzoneShare;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import java.util.ArrayList;

/**
 * QQ平台
 * 问题汇总：com.mTencentApi.tauth.AuthActivity需要添加（ <data android:scheme="tencent110557146" />）否则会一直返回分享取消
 * qq空间支持本地视频分享，网络视频使用web形式分享
 * qq好友不支持本地视频分享，支持网络视频分享
 *
 * Created by Jeeson on 2018/6/20.
 */
public class QQPlatform extends AbsPlatform {

    public static final String TAG = QQPlatform.class.getSimpleName();

    private Tencent mTencentApi;
    private QQLoginHelper mQQLoginHelper;
    private IUiListenerWrap mIUiListenerWrap;

    public static class Creator implements PlatformCreator {
        @Override
        public IPlatform create(Context context, int target) {
            IPlatform platform = null;
            SocialSDKConfig config = SocialSDK.getConfig();
            if (!CommonUtils.isAnyEmpty(config.getQqAppId(), config.getAppName())) {
                platform = new QQPlatform(context, config.getQqAppId(), config.getAppName());
            }
            return platform;
        }
    }


    QQPlatform(Context context, String appId, String appName) {
        super(context, appId, appName);
        mTencentApi = Tencent.createInstance(appId, context);
    }

    @Override
    public void initOnShareListener(OnShareListener listener) {
        super.initOnShareListener(listener);
        this.mIUiListenerWrap = new IUiListenerWrap(listener);
    }

    @Override
    public void recycle() {
        mTencentApi.releaseResource();
        mTencentApi = null;
    }

    @Override
    public boolean isInstall(Context context) {
        return mTencentApi.isQQInstalled(context);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_QQ_SHARE || requestCode == Constants.REQUEST_QZONE_SHARE) {
            if (mIUiListenerWrap != null && data != null)
                Tencent.handleResultData(data, mIUiListenerWrap);
        } else if (requestCode == Constants.REQUEST_LOGIN) {
            if (mQQLoginHelper != null)
                mQQLoginHelper.handleResultData(data);
        }
    }

    @Override
    public void login(Activity activity, OnLoginListener loginListener) {
        super.login(activity,loginListener);
        if (!mTencentApi.isSupportSSOLogin(activity)) {
            // 下载最新版
            loginListener.onFailure(new SocialError(SocialError.CODE_VERSION_LOW));
            return;
        }
        mQQLoginHelper = new QQLoginHelper(activity, mTencentApi, loginListener);
        mQQLoginHelper.login();
    }

    @Override
    public int getPlatformType() {
        return Target.PLATFORM_QQ;
    }


    private Bundle buildCommonBundle(String title, String summary, String targetUrl, int shareTarget) {
        final Bundle params = new Bundle();
        if (!TextUtils.isEmpty(title))
            params.putString(QQShare.SHARE_TO_QQ_TITLE, title);
        if (!TextUtils.isEmpty(summary))
            params.putString(QQShare.SHARE_TO_QQ_SUMMARY, summary);
        if (!TextUtils.isEmpty(targetUrl))
            params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, targetUrl);
        if (!TextUtils.isEmpty(mAppName))
            params.putString(QQShare.SHARE_TO_QQ_APP_NAME, mAppName);
        // 加了这个会自动打开qq空间发布
        if (shareTarget == Target.SHARE_QQ_ZONE)
            params.putInt(QQShare.SHARE_TO_QQ_EXT_INT, QQShare.SHARE_TO_QQ_FLAG_QZONE_AUTO_OPEN);
        return params;
    }

    @Override
    protected void shareOpenApp(int shareTarget, Activity activity, ShareObj obj) {
        boolean rst = CommonUtils.openApp(activity, SocialConstants.QQ_PKG);
        if (rst) {
            mOnShareListener.onSuccess();
        } else {
            mOnShareListener.onFailure(new SocialError("open app error"));
        }
    }


    @Override
    public void shareText(int shareTarget, Activity activity, ShareObj shareMediaObj) {
        if (shareTarget == Target.SHARE_QQ_FRIENDS) {
            shareTextByIntent(activity, shareMediaObj, SocialConstants.QQ_PKG, SocialConstants.QQ_FRIENDS_PAGE);
        } else if (shareTarget == Target.SHARE_QQ_ZONE) {
            final Bundle params = new Bundle();
            params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzonePublish.PUBLISH_TO_QZONE_TYPE_PUBLISHMOOD);
            params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, shareMediaObj.getSummary());
            mTencentApi.publishToQzone(activity, params, mIUiListenerWrap);
        }
    }

    @Override
    public void shareImage(int shareTarget, Activity activity, ShareObj shareMediaObj) {
        if (shareTarget == Target.SHARE_QQ_FRIENDS) {
            // 可以兼容分享图片和gif
            Bundle params = buildCommonBundle("", shareMediaObj.getSummary(), "", shareTarget);
            params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_IMAGE);
            params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, shareMediaObj.getThumbImagePath());
            mTencentApi.shareToQQ(activity, params, mIUiListenerWrap);
        } else if (shareTarget == Target.SHARE_QQ_ZONE) {
            final Bundle params = new Bundle();
            params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzonePublish.PUBLISH_TO_QZONE_TYPE_PUBLISHMOOD);
            params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, shareMediaObj.getSummary());
            ArrayList<String> imageUrls = new ArrayList<>();
            imageUrls.add(shareMediaObj.getThumbImagePath());
            params.putStringArrayList(QzonePublish.PUBLISH_TO_QZONE_IMAGE_URL, imageUrls);
            mTencentApi.publishToQzone(activity, params, mIUiListenerWrap);
        }
    }

    @Override
    public void shareApp(int shareTarget, Activity activity, ShareObj obj) {
        if (shareTarget == Target.SHARE_QQ_FRIENDS) {
            Bundle params = buildCommonBundle(obj.getTitle(), obj.getSummary(), obj.getTargetUrl(), shareTarget);
            params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_APP);
            if (!TextUtils.isEmpty(obj.getThumbImagePath()))
                params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, obj.getThumbImagePath());
            mTencentApi.shareToQQ(activity, params, mIUiListenerWrap);
        } else if (shareTarget == Target.SHARE_QQ_ZONE) {
            shareWeb(shareTarget, activity, obj);
        }
    }


    @Override
    public void shareWeb(int shareTarget, Activity activity, ShareObj obj) {
        if (shareTarget == Target.SHARE_QQ_FRIENDS) {
            // 分享图文
            final Bundle params = buildCommonBundle(obj.getTitle(), obj.getSummary(), obj.getTargetUrl(), shareTarget);
            params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
            // 本地或网络路径
            if (!TextUtils.isEmpty(obj.getThumbImagePath()))
                params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, obj.getThumbImagePath());
            mTencentApi.shareToQQ(activity, params, mIUiListenerWrap);
        } else {
            final ArrayList<String> imageUrls = new ArrayList<>();
            final Bundle params = new Bundle();
            params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT);
            params.putString(QzoneShare.SHARE_TO_QQ_APP_NAME, mAppName);
            params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, obj.getSummary());
            params.putString(QzoneShare.SHARE_TO_QQ_TITLE, obj.getTitle());
            params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, obj.getTargetUrl());
            imageUrls.add(obj.getThumbImagePath());
            params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, imageUrls);
            mTencentApi.shareToQzone(activity, params, mIUiListenerWrap);
        }
    }

    @Override
    public void shareMusic(int shareTarget, Activity activity, ShareObj obj) {
        if (shareTarget == Target.SHARE_QQ_FRIENDS) {
            Bundle params = buildCommonBundle(obj.getTitle(), obj.getSummary(), obj.getTargetUrl(), shareTarget);
            params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_AUDIO);
            if (!TextUtils.isEmpty(obj.getThumbImagePath()))
                params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, obj.getThumbImagePath());
            params.putString(QQShare.SHARE_TO_QQ_AUDIO_URL, obj.getMediaPath());
            mTencentApi.shareToQQ(activity, params, mIUiListenerWrap);
        } else if (shareTarget == Target.SHARE_QQ_ZONE) {
            shareWeb(shareTarget, activity, obj);
        }
    }

    @Override
    public void shareVideo(int shareTarget, Activity activity, ShareObj obj) {
        if (shareTarget == Target.SHARE_QQ_FRIENDS) {
            if (obj.isShareByIntent()) {
                shareVideoByIntent(activity, obj, SocialConstants.QQ_PKG, SocialConstants.QQ_FRIENDS_PAGE);
            } else {
                // 使用 web 格式分享
                SocialLogUtils.e(TAG, "qq不支持分享视频，使用web分享代替");
                obj.setTargetUrl(obj.getMediaPath());
                shareWeb(shareTarget, activity, obj);
            }
        } else if (shareTarget == Target.SHARE_QQ_ZONE) {
            // qq 空间支持本地文件发布
            if (!FileUtils.isHttpPath(obj.getMediaPath())) {
                SocialLogUtils.e(TAG, "qq空间本地视频分享");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        && activity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                    this.mIUiListenerWrap.onError(new SocialError("没有获取到读存储卡的权限，qq 空间分享本地视频功能无法继续"));
                    return;
                }
                final Bundle params = new Bundle();
                params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzonePublish.PUBLISH_TO_QZONE_TYPE_PUBLISHVIDEO);
                params.putString(QzonePublish.PUBLISH_TO_QZONE_VIDEO_PATH, obj.getMediaPath());
                mTencentApi.publishToQzone(activity, params, mIUiListenerWrap);
            } else {
                SocialLogUtils.e(TAG, "qq空间网络视频，使用web形式分享");
                shareWeb(shareTarget, activity, obj);
            }
        }
    }


    private class IUiListenerWrap implements IUiListener {

        private OnShareListener listener;

        IUiListenerWrap(OnShareListener listener) {
            this.listener = listener;
        }

        @Override
        public void onComplete(Object o) {
            if (listener != null)
                listener.onSuccess();
        }

        @Override
        public void onError(UiError uiError) {
            if (listener != null)
                listener.onFailure(new SocialError("分享失败 " + mQQLoginHelper.parseUiError(uiError)));
        }

        public void onError(SocialError e) {
            if (listener != null)
                listener.onFailure(e);
        }

        @Override
        public void onCancel() {
            if (listener != null)
                listener.onCancel();
        }
    }

}
