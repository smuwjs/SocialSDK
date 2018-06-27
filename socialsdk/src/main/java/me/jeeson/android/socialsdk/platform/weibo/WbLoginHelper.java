package me.jeeson.android.socialsdk.platform.weibo;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;

import me.jeeson.android.socialsdk.exception.SocialError;
import me.jeeson.android.socialsdk.utils.JsonUtils;
import me.jeeson.android.socialsdk.listener.OnLoginListener;
import me.jeeson.android.socialsdk.model.LoginResult;
import me.jeeson.android.socialsdk.platform.weibo.model.SinaAccessToken;
import me.jeeson.android.socialsdk.platform.weibo.model.SinaUser;

import me.jeeson.android.socialsdk.model.AccessToken;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WbAuthListener;
import com.sina.weibo.sdk.auth.WbConnectErrorMessage;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import me.jeeson.android.socialsdk.platform.Target;
import me.jeeson.android.socialsdk.utils.SocialLogUtils;


/**
 * 新浪微博登陆辅助
 * Created by Jeeson on 2018/6/20.
 */
class WbLoginHelper {

    public static final String TAG = WbLoginHelper.class.getSimpleName();

    private int mLoginType;
    private OnLoginListener mOnLoginListener;
    private SsoHandler      mSsoHandler;

    public WbLoginHelper(Activity context) {
        this.mSsoHandler = new SsoHandler(context);
        this.mLoginType = Target.LOGIN_WB;
    }

    /**
     * 获取用户信息
     *
     * @param token token
     */
    private void getUserInfo(final Oauth2AccessToken token) {
        JsonUtils.startJsonRequest("https://api.weibo.com/2/users/show.json?access_token=" + token.getToken() + "&uid=" + token.getUid(), SinaUser.class, new JsonUtils.Callback<SinaUser>() {
            @Override
            public void onSuccess(@NonNull SinaUser user) {
                SocialLogUtils.e(TAG, JsonUtils.getObject2Json(user));
                mOnLoginListener.onSuccess(new LoginResult(mLoginType, user, new SinaAccessToken(token)));
            }

            @Override
            public void onFailure(SocialError e) {
                mOnLoginListener.onFailure(e);
            }
        });
    }


    public void login(Activity activity, final OnLoginListener loginListener) {
        if (loginListener == null)
            return;
        this.mOnLoginListener = loginListener;
        justAuth(activity, new WbAuthListener() {
            @Override
            public void onSuccess(Oauth2AccessToken oauth2AccessToken) {
                getUserInfo(oauth2AccessToken);
            }

            @Override
            public void cancel() {
                loginListener.onCancel();
            }

            @Override
            public void onFailure(WbConnectErrorMessage msg) {
                loginListener.onFailure(new SocialError(msg.getErrorCode() + " " + msg.getErrorMessage()));
            }
        });
    }

    public void justAuth(final Activity activity, final WbAuthListener listener) {
        Oauth2AccessToken token = AccessToken.getToken(activity, AccessToken.SINA_TOKEN_KEY, Oauth2AccessToken.class);
        if (token != null && token.isSessionValid() && token.getExpiresTime() > System.currentTimeMillis()) {
            listener.onSuccess(token);
        } else {
            AccessToken.clearToken(activity, Target.LOGIN_WB);
            mSsoHandler.authorize(new WbAuthListener() {
                @Override
                public void onSuccess(Oauth2AccessToken oauth2AccessToken) {
                    oauth2AccessToken.setBundle(null);

                    SocialLogUtils.json("test", oauth2AccessToken.toString());
                    AccessToken.saveToken(activity, AccessToken.SINA_TOKEN_KEY, oauth2AccessToken);
                    listener.onSuccess(oauth2AccessToken);
                }

                @Override
                public void cancel() {
                    listener.cancel();
                }

                @Override
                public void onFailure(WbConnectErrorMessage wbConnectErrorMessage) {
                    listener.onFailure(wbConnectErrorMessage);
                }
            });
        }
    }

    public void authorizeCallBack(int requestCode, int resultCode, Intent data) {
        if (mSsoHandler != null) {
            mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
        }
    }
}
