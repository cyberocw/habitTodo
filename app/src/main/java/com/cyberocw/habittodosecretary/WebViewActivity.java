package com.cyberocw.habittodosecretary;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
    @BindView(R.id.progressBar)
    ProgressBar mProgressBar;

    @OnClick(R.id.btnClose) void submit() {
        finish();
    }
    @OnClick(R.id.btnOption) void showOption(View v){
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.browser_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.menu_share :
                        showSharePopup();
                        return true;
                    case R.id.menu_reload :
                        reloadPage();
                        return true;
                    case R.id.menu_back :
                        moveBack();
                        return true;
                }
                return false;
            }
        });
        popup.show();
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
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                if (!TextUtils.isEmpty(title)) {
                    WebViewActivity.this.setTitle(title);
                }
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if(newProgress < 100){
                    mProgressBar.setVisibility(View.VISIBLE);
                    mProgressBar.setProgress(newProgress);
                }else{
                    mProgressBar.setVisibility(View.INVISIBLE);
                }
            }
        });
        webView.setWebViewClient(new WebViewClient());

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.browser_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_share:
                showSharePopup();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void showSharePopup(){
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TITLE, webView.getTitle());
        shareIntent.putExtra(Intent.EXTRA_TEXT, webView.getUrl());
        startActivity(Intent.createChooser(shareIntent, "Share..."));
    }

    public void setTitle(String title){
        mTvTitle.setText(title);
        mTvUrl.setText(webView.getUrl());
    }

    public void reloadPage(){
        webView.reload();
    }

    public void moveBack(){
        if(webView.canGoBack())
            webView.goBack();
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
