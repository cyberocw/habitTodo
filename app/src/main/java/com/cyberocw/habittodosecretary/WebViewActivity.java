package com.cyberocw.habittodosecretary;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.fabric.sdk.android.Fabric;

/**
 * Created by cyber on 2017-07-09.
 */

public class WebViewActivity extends AppCompatActivity {
    Context mCtx;
    Bundle mBundle;

    @BindView(R.id.adView)	AdView adView;
    @BindView(R.id.webView) WebView webView;
    @BindView(R.id.webViewTitle) TextView mTvTitle;
    @BindView(R.id.webViewUrl) TextView mTvUrl;

    @OnClick(R.id.btnClose) void submit() {
        Log.d(Const.DEBUG_TAG, "btn close click");
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        int alarmOption = -1;
        Log.d(this.toString(), " oncreated ocwocw" + intent.getExtras());

        super.onCreate(savedInstanceState);


        setContentView(R.layout.fragment_webview);

        ButterKnife.bind(this);
        mCtx = getApplicationContext();

        mBundle = intent.getExtras();

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                WebViewActivity.this.setTitle(view.getTitle());
            }
        });


        if(mBundle != null) {
            //webView.setWebChromeClient(new WebChromeClient());
            webView.loadUrl(mBundle.getString("url"));
        }

        //AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("048A3A6B542D3DD340272D8C1D80AC18")
                .build();
        if(Const.IS_DEBUG){
            //adView.setVisibility(View.GONE);
            //+dimension margin 0 주기
        }
        adView.loadAd(adRequest);

        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        Fabric.with(this, new Crashlytics());
    }

    public void setTitle(String title){
        mTvTitle.setText(title);
        mTvUrl.setText(webView.getUrl());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
