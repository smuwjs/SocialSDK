package me.jeeson.android.demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.RadioGroup;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.jeeson.android.socialsdk.exception.SocialError;
import me.jeeson.android.socialsdk.listener.OnLoginListener;
import me.jeeson.android.socialsdk.listener.OnShareListener;
import me.jeeson.android.socialsdk.listener.impl.SimpleShareListener;
import me.jeeson.android.socialsdk.manager.SocialLoginManager;
import me.jeeson.android.socialsdk.manager.SocialShareManager;
import me.jeeson.android.socialsdk.model.LoginResult;
import me.jeeson.android.socialsdk.model.ShareObj;
import me.jeeson.android.socialsdk.platform.Target;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.radioGShareMedia)
    RadioGroup radioGShareMedia;

    @BindView(R.id.radioGSharePlatform)
    RadioGroup radioGSharePlatform;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    /**
     * 微信登录
     */
    @OnClick(R.id.btnWXLogin)
    public void onWXLogin() {
        SocialLoginManager.login(this, Target.LOGIN_WX, onLoginListener);
    }

    /**
     * qq登录
     */
    @OnClick(R.id.btnQQLogin)
    public void onQQLogin() {
        SocialLoginManager.login(this, Target.LOGIN_QQ, onLoginListener);
    }

    /**
     * 新浪微博登录
     */
    @OnClick(R.id.btnSinaWBLogin)
    public void onSinaWBLogin() {
        SocialLoginManager.login(this, Target.LOGIN_WB, onLoginListener);
    }

    OnLoginListener onLoginListener = new OnLoginListener() {
        @Override
        public void onStart() {
            showToast("开始授权登录");
        }

        @Override
        public void onSuccess(LoginResult loginResult) {
            showToast("登录成功" + loginResult);
        }

        @Override
        public void onCancel() {
            showToast("取消登录");
        }

        @Override
        public void onFailure(SocialError e) {
            showToast("登录失败");
        }
    };

    OnShareListener onShareListener = new SimpleShareListener() {
        @Override
        public void onStart(int shareTarget, ShareObj obj) {
            showToast("开始分享");
        }

        @Override
        public void onSuccess() {
            showToast("分享成功");
        }

        @Override
        public void onFailure(SocialError e) {
            showToast("分享失败");
        }

        @Override
        public void onCancel() {
            showToast("取消分享");
        }
    };

    private void showToast(String toast) {
        Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
    }


    @OnClick(R.id.btnShare)
    public void onShare() {

        ShareObj shareObj;
        int shareTarget;

        // 测试用的路径
        String netVideoPath = "http://7xtjec.com1.z0.glb.clouddn.com/export.mp4";
        String netImagePath = "http://7xtjec.com1.z0.glb.clouddn.com/token.png";
        String netMusicPath = "http://7xtjec.com1.z0.glb.clouddn.com/test_music.mp3";
        String targetUrl = "http://bbs.csdn.net/topics/391545021";

        //获取分享类型
        switch (radioGShareMedia.getCheckedRadioButtonId()) {
            case R.id.radioShareText:
                shareObj = ShareObj.buildTextObj("分享文字", "summary");
                break;

            case R.id.radioShareImage:
                shareObj = ShareObj.buildImageObj(netImagePath);
                break;

            case R.id.radioShareTextImage:
                shareObj = ShareObj.buildImageObj( netImagePath ,"分享图片");
                break;

            case R.id.radioShareMusic:
                shareObj = ShareObj.buildMusicObj("分享音乐", "summary", netImagePath, targetUrl, netMusicPath, 10);
                break;

            case R.id.radioShareVideo:
                shareObj = ShareObj.buildVideoObj("分享视频", "summary", netImagePath, targetUrl, netVideoPath, 10);
                break;

            case R.id.radioShareWeb:
                shareObj = ShareObj.buildWebObj("分享web", "summary", netImagePath, targetUrl);
                break;

            default:
                return;
        }

        //分享渠道
        switch (radioGSharePlatform.getCheckedRadioButtonId()) {
            case R.id.radioShareWX:
                shareTarget = Target.SHARE_WX_FRIENDS;
                break;

            case R.id.radioShareWXCircle:
                shareTarget = Target.SHARE_WX_ZONE;
                break;

            case R.id.radioShareQQ:
                shareTarget = Target.SHARE_QQ_FRIENDS;
                break;

            case R.id.radioShareQZone:
                shareTarget = Target.SHARE_QQ_ZONE;
                break;

            case R.id.radioShareSinaWB:
                shareTarget = Target.SHARE_WB_NORMAL;
                break;

            default:
                return;
        }

        SocialShareManager.share(this,  shareTarget, shareObj, onShareListener);
    }
}
