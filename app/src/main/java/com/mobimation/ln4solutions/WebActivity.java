package com.mobimation.ln4solutions;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ZoomButtonsController;

public class WebActivity extends Activity {
    private final String TAG = WebActivity.class.getSimpleName();
    String webUrl;
    WebView browser;
    int click=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        Intent passed = getIntent(); // gets the previously created intent
        webUrl = passed.getStringExtra("webUrl");
        browser = (WebView)findViewById(R.id.webView);
        browser.getSettings().setSupportZoom(true);
        browser.getSettings().setBuiltInZoomControls(true);
   /*
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            // Use the API 11+ calls to disable the controls
            // Use a seperate class to obtain 1.6 compatibility
            new Runnable() {
                public void run() {
                    browser.getSettings().setDisplayZoomControls(false);
                }
            }.run();
        } else {
            try {
                final ZoomButtonsController zoom_controll =
                        (ZoomButtonsController) browser.getClass().getMethod("getZoomButtonsController").invoke(browser, null);
                zoom_controll.getContainer().setVisibility(View.GONE);
            }
            catch (java.lang.NoSuchMethodException nsm) {
                nsm.printStackTrace();
            }
            catch (java.lang.IllegalAccessException iae) {

            }
            catch (java.lang.reflect.InvocationTargetException ite) {

            }
        }  */
        browser.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        browser.setScrollbarFadingEnabled(true);
        browser.getSettings().setLoadsImagesAutomatically(true);
        browser.getSettings().setJavaScriptEnabled(true);

        browser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                click=0;
            }
        });
        browser.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                click++;
                if (click>2) {
                    click=0;
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
