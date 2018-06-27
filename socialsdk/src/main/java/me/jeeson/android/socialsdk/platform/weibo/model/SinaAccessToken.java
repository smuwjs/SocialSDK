package me.jeeson.android.socialsdk.platform.weibo.model;

import com.sina.weibo.sdk.auth.Oauth2AccessToken;

import me.jeeson.android.socialsdk.model.AccessToken;
import me.jeeson.android.socialsdk.platform.Target;

/**
 * 新浪的token
 * Created by Jeeson on 2018/6/20.
 */
public class SinaAccessToken extends AccessToken {

    private String refresh_token;
    private String phone;

    public SinaAccessToken(Oauth2AccessToken token) {
        this.setOpenid(token.getUid());
        this.setAccess_token(token.getToken());
        this.setExpires_in(token.getExpiresTime());
        this.refresh_token = token.getRefreshToken();
        this.phone = token.getPhoneNum();
    }

    public String getRefresh_token() {
        return refresh_token;
    }

    public void setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }


    @Override
    public int getLoginTarget() {
        return Target.LOGIN_WB;
    }
}
