package com.example.uclab.smarttest;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.apache.http.NameValuePair;

import java.util.ArrayList;

import kr.ac.sch.se.Common.SessionIDSharedPreference;

public class Main_Login extends AppCompatActivity {
    ArrayList<NameValuePair> params;
    private String TAG = "MAIN_LOGIN_BLUECORE";
    private WebView mWebView;
    private final String url1 = "http://welltec.blue-core.com:10000/openapi/mobile/login?done_url=activity://ecg_scale_menu";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWebView = (WebView)findViewById(R.id.bluecore_login_Webview);

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.loadUrl(url1);
        mWebView.setWebViewClient(new WebViewClientClass());
    }

    private class WebViewClientClass extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, final String url) {

            if (url != null && url.startsWith("activity://")) {
                try {
                    Log.e(TAG, "url: "+url);
                    Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                    Intent existPackage = getPackageManager().getLaunchIntentForPackage(intent.getPackage());

                    if (existPackage != null) {
                        int index = url.indexOf("sid");
                        String sid = url.substring(index+4);
                        SessionIDSharedPreference.saveSessionIdPreference(getApplicationContext(), sid);
                        Log.e(TAG, "sid: " + sid);
                        startActivity(intent);
                    }
                    return true;
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
            view.loadUrl(url);
            return false;
        }
    }
}
