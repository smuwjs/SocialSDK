package me.jeeson.android.socialsdk.manager;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import me.jeeson.android.socialsdk.SocialSDK;
import me.jeeson.android.socialsdk.common.SocialConstants;
import me.jeeson.android.socialsdk.exception.SocialError;
import me.jeeson.android.socialsdk.listener.OnShareListener;
import me.jeeson.android.socialsdk.model.ShareObj;
import me.jeeson.android.socialsdk.platform.IPlatform;
import me.jeeson.android.socialsdk.platform.Target;
import me.jeeson.android.socialsdk.uikit.ActionActivity;
import me.jeeson.android.socialsdk.utils.CommonUtils;
import me.jeeson.android.socialsdk.utils.FileUtils;
import me.jeeson.android.socialsdk.utils.SocialLogUtils;
import me.jeeson.android.socialsdk.utils.ShareObjCheckUtils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;

import static me.jeeson.android.socialsdk.manager.SocialPlatformManager.KEY_ACTION_TYPE;

/**
 * 分享管理类，使用该类进行分享操作
 * Created by Jeeson on 2018/6/20.
 */
public class SocialShareManager {

    public static final String TAG = SocialShareManager.class.getSimpleName();

    static OnShareListener sListener;

    /**
     * 开始分享，供外面调用
     *
     * @param context         context
     * @param shareTarget     分享目标
     * @param shareObj        分享对象
     * @param onShareListener 分享监听
     */
    public static void share(final Context context, @Target.ShareTarget final int shareTarget,
            final ShareObj shareObj, final OnShareListener onShareListener) {
        if(onShareListener != null) {
            onShareListener.onStart(shareTarget, shareObj);
        }
        Task.callInBackground(new Callable<ShareObj>() {
            @Override
            public ShareObj call() throws Exception {
                prepareImageInBackground(context, shareObj);
                ShareObj temp = null;
                try {
                    temp = onShareListener.onPrepareInBackground(shareTarget, shareObj);
                } catch (Exception e) {
                    SocialLogUtils.t(e);
                }
                if (temp != null) {
                    return temp;
                } else {
                    return shareObj;
                }
            }
        }).continueWith(new Continuation<ShareObj, Boolean>() {
            @Override
            public Boolean then(Task<ShareObj> task) throws Exception {
                if (task.isFaulted() || task.getResult() == null) {
                    if (onShareListener != null) {
                        SocialError exception = new SocialError("onPrepareInBackground error", task.getError());
                        onShareListener.onFailure(exception);
                    }
                    return null;
                }
                doShare(context, shareTarget, task.getResult(), onShareListener);
                return true;
            }
        }, Task.UI_THREAD_EXECUTOR).continueWith(new Continuation<Boolean, Boolean>() {
            @Override
            public Boolean then(Task<Boolean> task) throws Exception {
                if (task.isFaulted()) {
                    SocialError exception = new SocialError("SocialShareManager.share() error", task.getError());
                    onShareListener.onFailure(exception);
                }
                return true;
            }
        });
    }

    // 如果是网络图片先下载
    private static void prepareImageInBackground(Context context, ShareObj shareObj) throws SocialError {
        String thumbImagePath = shareObj.getThumbImagePath();
        // 图片路径为网络路径，下载为本地图片
        if (!TextUtils.isEmpty(thumbImagePath) && FileUtils.isHttpPath(thumbImagePath)) {
            if (!FileUtils.hasStoragePermission(context)) {
                throw new SocialError(SocialError.CODE_STORAGE_ERROR, "没有读写存储的权限");
            }
            File file = SocialSDK.getRequestAdapter().getFile(thumbImagePath);
            if (FileUtils.isExist(file)) {
                shareObj.setThumbImagePath(file.getAbsolutePath());
            }
        }
    }


    // 开始分享
    @TargetApi(Build.VERSION_CODES.ECLAIR)
    private static boolean doShare(Context context, @Target.ShareTarget int shareTarget, ShareObj shareObj, OnShareListener onShareListener) {
        if (!ShareObjCheckUtils.checkObjValid(shareObj, shareTarget)) {
            onShareListener.onFailure(new SocialError(SocialError.CODE_SHARE_OBJ_VALID));
            return true;
        }
        sListener = onShareListener;
        IPlatform platform = SocialPlatformManager.makePlatform(context, shareTarget);
        if (!platform.isInstall(context)) {
            onShareListener.onFailure(new SocialError(SocialError.CODE_NOT_INSTALL));
            return true;
        }
        Intent intent = new Intent(context, ActionActivity.class);
        intent.putExtra(SocialPlatformManager.KEY_ACTION_TYPE, SocialPlatformManager.ACTION_TYPE_SHARE);
        intent.putExtra(SocialPlatformManager.KEY_SHARE_MEDIA_OBJ, shareObj);
        intent.putExtra(SocialPlatformManager.KEY_SHARE_TARGET, shareTarget);
        context.startActivity(intent);
        if (context instanceof Activity) {
            ((Activity)context).overridePendingTransition(0, 0);
        }
        return false;
    }

    /**
     * 激活分享
     *
     * @param activity activity
     */
    static void _actionShare(Activity activity) {
        Intent intent = activity.getIntent();
        int actionType = intent.getIntExtra(KEY_ACTION_TYPE, SocialPlatformManager.INVALID_PARAM);
        int shareTarget = intent.getIntExtra(SocialPlatformManager.KEY_SHARE_TARGET, SocialPlatformManager.INVALID_PARAM);
        ShareObj shareObj = intent.getParcelableExtra(SocialPlatformManager.KEY_SHARE_MEDIA_OBJ);
        if (actionType != SocialPlatformManager.ACTION_TYPE_SHARE)
            return;
        if (shareTarget == SocialPlatformManager.INVALID_PARAM) {
            SocialLogUtils.e(TAG, "shareTargetType无效");
            return;
        }
        if (shareObj == null) {
            SocialLogUtils.e(TAG, "shareObj == null");
            return;
        }
        if (sListener == null) {
            SocialLogUtils.e(TAG, "请设置 OnShareListener");
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && activity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            SocialLogUtils.e(TAG, "没有获取到读存储卡的权限，这可能导致某些分享不能进行");
        }
        if (SocialPlatformManager.getPlatform() == null)
            return;
        SocialPlatformManager.getPlatform().initOnShareListener(new FinishShareListener(activity));
        SocialPlatformManager.getPlatform().share(activity, shareTarget, shareObj);
    }

    static class FinishShareListener implements OnShareListener {

        private WeakReference<Activity> mActivityRef;

        FinishShareListener(Activity activity) {
            mActivityRef = new WeakReference<>(activity);
        }

        @Override
        public void onStart(int shareTarget, ShareObj obj) {
            if (sListener != null) sListener.onStart(shareTarget, obj);
        }

        @Override
        public ShareObj onPrepareInBackground(int shareTarget, ShareObj obj) throws Exception {
            if (sListener != null)
                return sListener.onPrepareInBackground(shareTarget, obj);
            return null;
        }

        private void finish() {
            SocialPlatformManager.release(mActivityRef.get());
            sListener = null;
        }

        @Override
        public void onSuccess() {
            if (sListener != null) sListener.onSuccess();
            finish();
        }


        @Override
        public void onCancel() {
            if (sListener != null) sListener.onCancel();
            finish();
        }


        @Override
        public void onFailure(SocialError e) {
            if (sListener != null) sListener.onFailure(e);
            finish();
        }
    }


    /**
     * 发送短信分享
     *
     * @param context ctx
     * @param phone   手机号
     * @param msg     内容
     */
    public static void sendSms(Context context, String phone, String msg) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (TextUtils.isEmpty(phone))
            phone = "";
        intent.setData(Uri.parse("smsto:" + phone));
        intent.putExtra("sms_body", msg);
        intent.setType("vnd.android-dir/mms-sms");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 发送邮件分享
     *
     * @param context ctx
     * @param mailto  email
     * @param subject 主题
     * @param msg     内容
     */
    public static void sendEmail(Context context, String mailto, String subject, String msg) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        if (TextUtils.isEmpty(mailto))
            mailto = "";
        intent.setData(Uri.parse("mailto:" + mailto));
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, msg);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 打开平台 app
     *
     * @param context ctx
     * @param target  平台
     * @return 是否成功打开
     */
    public static boolean openApp(Context context, @Target.ShareTarget int target) {
        String pkgName = null;
        switch (target) {
            case Target.SHARE_QQ_FRIENDS:
            case Target.SHARE_QQ_ZONE:
                pkgName = SocialConstants.QQ_PKG;
                break;
            case Target.SHARE_WX_FRIENDS:
            case Target.SHARE_WX_ZONE:
            case Target.SHARE_WX_FAVORITE:
                pkgName = SocialConstants.WECHAT_PKG;
                break;
            case Target.SHARE_WB_NORMAL:
            case Target.SHARE_WB_OPENAPI:
                pkgName = SocialConstants.SINA_PKG;
                break;
        }
        return !TextUtils.isEmpty(pkgName) && CommonUtils.openApp(context, pkgName);
    }
}
