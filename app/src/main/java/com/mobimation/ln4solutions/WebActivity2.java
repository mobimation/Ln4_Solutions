package com.mobimation.ln4solutions;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

public class WebActivity2 extends Activity {
    /**
     * Modified for rotation support
     * where the browser is to retain its content
     * instead of reloading.
     */
    String webUrl;
    private final String TAG = WebActivity2.class.getSimpleName();
    int click=0;
    protected FrameLayout webViewPlaceholder;
    protected WebView browser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web2);

        Intent passed = getIntent(); // gets the previously created intent
        webUrl = passed.getStringExtra("webUrl");



        initUi();
    }

    protected void initUi()
    {
        // Retrieve UI elements
        webViewPlaceholder = ((FrameLayout)findViewById(R.id.webViewPlaceholder));
        if (browser==null) {

            browser = new WebView(this);
            browser.setLayoutParams(
                    new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                            ViewGroup.LayoutParams.FILL_PARENT));
            browser.getSettings().setSupportZoom(true);
            browser.getSettings().setBuiltInZoomControls(true);
            browser.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
            browser.setScrollbarFadingEnabled(true);
            browser.getSettings().setLoadsImagesAutomatically(true);
            browser.getSettings().setJavaScriptEnabled(true);

            browser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    click = 0;
                }
            });
            browser.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    click++;
                    if (click > 2) {
                        click = 0;
                        // TEST: Three successive long presses: Abandon page view and return to login screen
                        final SharedPreferences pref = getApplicationContext().getSharedPreferences(
                                "com.mobimation.URL", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = pref.edit();
                        editor.clear();
                        editor.commit();
                        Log.d(TAG, "TEST:Cleared granted URL");
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent);
                    }
                    return true;
                }
            });

            browser.setWebViewClient(new WebViewClient());
 //           if (savedInstanceState == null)
            browser.loadUrl(webUrl);
            webViewPlaceholder.addView(browser);
        }
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        if (browser != null)
        {
            // Remove the WebView from the old placeholder
            webViewPlaceholder.removeView(browser);
        }

        super.onConfigurationChanged(newConfig);

        // Load the layout resource for the new configuration
        setContentView(R.layout.activity_web2);

        // Reinitialize the UI
        initUi();
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
