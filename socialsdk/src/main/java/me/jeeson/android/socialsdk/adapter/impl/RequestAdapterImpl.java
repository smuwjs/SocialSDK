package me.jeeson.android.socialsdk.adapter.impl;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import me.jeeson.android.socialsdk.adapter.IRequestAdapter;
import me.jeeson.android.socialsdk.utils.FileUtils;
import me.jeeson.android.socialsdk.utils.SocialLogUtils;
import me.jeeson.android.socialsdk.utils.StreamUtils;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * 请求 adapter
 *
 * Created by Jeeson on 2018/6/20.
 */
public class RequestAdapterImpl implements IRequestAdapter {

    private HttpURLConnection mConnection;

    @Override
    public File getFile(String url) {
        if(TextUtils.isEmpty(url) || !url.startsWith("http"))
            return null;
        File file = null;
        try {
            file = new File(FileUtils.mapUrl2LocalPath(url));
            if (!FileUtils.isExist(file)) {
                return StreamUtils.saveStreamToFile(file, openStream(url));
            }
        } catch (Exception e) {
            SocialLogUtils.e(e);
        } finally {
            close();
        }
        return file;
    }

    @Override
    public String getJson(String url) {
        if(TextUtils.isEmpty(url) || !url.startsWith("http"))
            return null;
        try {
            return StreamUtils.saveStreamToString(openStream(url));
        } catch (Exception e) {
            SocialLogUtils.e(e);
        } finally {
            close();
        }
        return null;
    }

    // 关闭连接
    private void close() {
        if (mConnection != null) {
            mConnection.disconnect();
        }
    }

    // 开启连接，打开流
    private InputStream openStream(String url) throws Exception {
        mConnection = (HttpURLConnection) new URL(url).openConnection();
        if (mConnection instanceof HttpsURLConnection) {
            initHttpsConnection(mConnection);
        }
        return StreamUtils.openHttpStream(mConnection);
    }

    /**
     * 针对 https 连接配置
     *
     * @param conn 连接
     * @throws Exception e
     */
    private void initHttpsConnection(HttpURLConnection conn) throws Exception {
        HttpsURLConnection httpsURLConnection = (HttpsURLConnection) conn;
        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, new TrustManager[]{new MyTrustManager()}, new SecureRandom());
        httpsURLConnection.setSSLSocketFactory(sc.getSocketFactory());
        httpsURLConnection.setHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return hostname != null;
            }
        });
    }


    @SuppressLint("TrustAllX509TrustManager")
    private class MyTrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {

        }

        @Override
        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {

        }

        @Override
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }
}
