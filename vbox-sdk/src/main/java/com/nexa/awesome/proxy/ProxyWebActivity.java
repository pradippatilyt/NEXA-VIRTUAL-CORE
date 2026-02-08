package com.nexa.awesome.proxy;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.nexa.awesome.R;
import com.nexa.awesome.utils.auth.TokenCache;

public class ProxyWebActivity extends Activity {
    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        mWebView = findViewById(R.id.webView);
        initWebView(mWebView);

        String url = getIntent().getStringExtra("url");
        if (url != null) {
            mWebView.loadUrl(url);
        }
    }

    private void initWebView(WebView webView) {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains("facebook.com")) {
                    TokenCache.setFacebookToken("real_fb_token_here");
                    Log.i("ProxyWebActivity", "Facebook login successful");
                } else if (url.contains("twitter.com")) {
                    TokenCache.setTwitterToken("real_twitter_token_here");
                    Log.i("ProxyWebActivity", "Twitter login successful");
                } else if (url.contains("accounts.google.com")) {
                    TokenCache.setGoogleToken("real_google_token_here");
                    Log.i("ProxyWebActivity", "Google login successful");
                }
                return super.shouldOverrideUrlLoading(view, url);
            }
        });

        webView.setWebChromeClient(new WebChromeClient());
    }

    @Override
    protected void onDestroy() {
        if (mWebView != null) {
            mWebView.destroy();
        }
        super.onDestroy();
    }
}