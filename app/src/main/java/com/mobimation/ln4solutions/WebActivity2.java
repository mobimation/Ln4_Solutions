package com.mobimation.ln4solutions;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

public class WebActivity2 extends Activity {
    /**
     * Modified for rotation support
     */
    String webUrl;
    protected FrameLayout webViewPlaceholder;
    protected WebView browser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        Intent passed = getIntent(); // gets the previously created intent
        webUrl = passed.getStringExtra("webUrl");
        browser = (WebView)findViewById(R.id.webView);
        browser.getSettings().setJavaScriptEnabled(true);
        browser.setWebViewClient(new WebViewClient());
        if (savedInstanceState == null)
            browser.loadUrl(webUrl);
    }
    @Override
    protected void onSaveInstanceState(Bundle outState )
    {
        super.onSaveInstanceState(outState);
        browser.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        browser.restoreState(savedInstanceState);



    }
}
