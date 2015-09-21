package com.mobimation.ln4solutions;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Experimental version of LoginActivity that makes use of the
 * custom formatting CustomerId widget.
 */
public class LoginActivity2 extends Activity {
    final static boolean auto=true; // true = Automatic browser selection

    protected Button buttonSubmit;
    protected ImageView buttonUpgrade;
    protected TextView status;
    private final String LTAG = LoginActivity2.class.getSimpleName();
    // url to ln4 customer authentication service
    private static String url="https://h01.ln4solutions.com/code2url/code2url.php";
    private String webUrl;
    private String befo;
    private int count=0;
    private int aft;
    private int bef;
    private boolean forward;
    private boolean add;
    private boolean adjust;
/*
    @Override
    public void onStart() {
        super.onStart();
        final View decorView = getWindow().getDecorView();
        if (Build.VERSION.SDK_INT >= 11)
        decorView.setOnSystemUiVisibilityChangeListener(
                new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int i) {
                        int height = decorView.getHeight();
                        Log.i(LTAG, "Current height: " + height);
                    }
                });
    }
*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login2);
        // Temporary: When user clicks LN4 logo a new app version
        // is installed if any upon user approval.

        /*  TODO Experimental update function disabled for now
        buttonUpgrade = (ImageView) findViewById(R.id.logo);
        buttonUpgrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Launch application update activity
                Intent intent = new Intent(getApplicationContext(),
                        UpdateActivity.class);
                startActivity(intent);
            }
        });
        */

        buttonSubmit = (Button)findViewById(R.id.buttonSubmit);
        status = (TextView)findViewById(R.id.status);
        status.setText(" ");
        final CustomerId customerId = (CustomerId)findViewById(R.id.customerId2);
        // Check if an id is already authenticated and if so fetch customer url
        // and launch browser.
        final SharedPreferences pref = this.getSharedPreferences(
                "com.mobimation.URL", Context.MODE_PRIVATE);
        // Use browser preference setting if any (internal=default)
        boolean external=pref.getBoolean("external", false);
        String id=pref.getString("id", null);
        if (id!=null) {
            // We have an approved customer id, retrieve url and launch browser
            new Authenticator(getApplicationContext()).execute(url, id);

/*
            if (auto)
                launchWebAuto(getApplicationContext(),u);
            else
                launchWeb(external, getApplicationContext(), u);
*/
            finish(); // Abort Login activity launch
        }
        else {
            /**
             * Request user input of Customer Id, with proper reformatting of
             * the EditText content during user input.
             */
            customerId.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    add=false;
                    adjust=false;
                    if (after>count) { // moving forward
                        if ((s.length() == 2) || (s.length() == 6)) {
                            add = true;
                        }
                    }
                    aft=after;

                       befo=s.toString();
                    Log.d(LTAG,"Before="+"["+s+"]"+" start="+start+" count="+count+" after="+after);
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                   Log.d(LTAG,"On="+"["+s+"]"+" start="+start+" before="+before+" count="+count);
                    if (count>0) {
                        forward = true;
                        // Semi intelligent space formatting for dummies
                        if (s.length() == 4) {
                            if (start == 3) if (before == 0) if (count == 1) {
                                customerId.setText(customerId.getText().insert(3," "));
                                customerId.setSelection(customerId.length());
                            }
                        }
                        else
                        if (s.length() == 8) {
                            if (start == 7) if (before == 0) if (count == 1) {
                                customerId.setText(customerId.getText().insert(7," "));
                                customerId.setSelection(customerId.length());
                            }
                        }
                    }
                    else
                        forward=false;
                }

                @Override
                public void afterTextChanged(Editable s) {
                    Log.d(LTAG,"After="+"["+s.toString()+"]");
                    if (add)
                        customerId.setText(customerId.getText() + " ");
                    customerId.setSelection(customerId.length());

                    if (s.length()>11)
                        s.delete(s.length() - 1, s.length());

                    if (s.length()==11) {// Take VK down
                            VKdown();
                    }
                }
            });

            /**
             * When the user clicks the Submit button
             * the current content of edited customer id is submitted
             * for authentication. The async task that
             * carry out this job will take care of launching
             * the browser with the given url in case the given
             * id was approved.
             */
            buttonSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Run asynchronous thread to authenticate submitted id string
                    String id = customerId.getText().toString();
                    id=id.replace(" ","");  // Clean up
                    // Run authentication/launch thread
                    new Authenticator(getApplicationContext()).execute(url, id);
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.menu_login, menu);

        int positionOfMenuItem = 0; // or whatever...
        MenuItem item = menu.getItem(positionOfMenuItem);
       SpannableString s = new SpannableString("Settings");
        s.setSpan(new ForegroundColorSpan(Color.parseColor("#DDDDDD")), 0, s.length(), 0);
        item.setTitle(s);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

         if (id == R.id.action_settings) {
             Intent myIntent = new Intent(this,SettingsActivity.class);
             startActivity(myIntent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

/*
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            final View decorView = getWindow().getDecorView();
            if (Build.VERSION.SDK_INT >= 11)
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);}
    }
*/
    private void VKdown() {
        InputMethodManager inputMethodManager =
                (InputMethodManager) getApplicationContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * Run asynchronous customer id authentication over https to ln4 server.
     * In case id is approved a corresponding customer url is returned.
     * The browser selected for the device used will be launched to
     * retrieve/present the web content for that url.
     */
    protected class Authenticator extends AsyncTask<String, String, String> {

        private final String TAG = Authenticator.class.getSimpleName();
        private Context context;
        private String code;

        public String convertStreamToString(InputStream is) {
    /*
     * To convert the InputStream to String we use the BufferedReader.readLine()
     * method. We iterate until the BufferedReader return null which means
     * there's no more data to read. Each line will appended to a StringBuilder
     * and returned as String.
     */
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();

            String line = null;
            try {
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return sb.toString();
        }

        protected Authenticator(Context context) {
            this.context = context;
        }

        @Override
        protected  void onPreExecute()
        {
            // Erase any stored authentication
            SharedPreferences pref = context.getSharedPreferences(
                    "com.mobimation.URL", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.clear();
            editor.commit();
        }
        @Override
        protected String doInBackground(String... params) {
            String webUrl=null;
            Log.d(TAG, "doInBackground");
            publishProgress("Submitting..");

           /**
            * Submit JSON request over https to Ln4 Solutions server
            */
            code=params[1];
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(params[0]);
            try {
                JSONObject json = new JSONObject();
                json.put("code", code);
                HttpPost request = new HttpPost(params[0]);
                request.setEntity(new ByteArrayEntity(json.toString().getBytes(
                        "UTF8")));
                HttpResponse response = client.execute(request);
                BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                String line = "";
                line = rd.readLine();
                JSONObject jsonObj = new JSONObject(line);
                String result=jsonObj.getString("result");
                if (result.equals("Success")) {
                    webUrl = jsonObj.getString("url");
                    publishProgress("Success");
                }
                else {
                    webUrl=null;
                    publishProgress("Invalid Customer Id");
                    Log.e(TAG,"Invalid Customer Id");
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            catch (JSONException je) {
                je.printStackTrace();
            }
            return webUrl;  // onPostExecute() receives result..
        }

        /**
         * publishProgress() updates status string
         * @param values Tell user how the background job is progressing
         */
        @Override
        protected void onProgressUpdate(String... values) {
            status.setText(values[0]);
        }

        /**
         * Called when doInBackground() completes.
         * Takes care of launching a browser with the given url
         * @param webUrl The url to view; null if invalid customer id
         */
        @Override
        protected void onPostExecute(String webUrl) {
            if (webUrl!=null) {  // If web access granted
                // Store customer id and page URL
                SharedPreferences pref = context.getSharedPreferences(
                        "com.mobimation.URL", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("url", webUrl);  //Save customer url
                editor.putString("id",code);      // Save customer id
                editor.commit();
                if (auto)
                    launchWebAuto(context,webUrl);
                else {
                    // Use browser preference setting if any (internal=default)
                    boolean external = pref.getBoolean("external", false);
                    // Launch WebActivity
                    Log.d(TAG, "Launching web activity");
                    status.setText("Loading browser..");
                    launchWeb(external, context, webUrl);
                }
            }
            else
                status.setText("Customer id "+code+" not valid");
        }
    }

    /**
     * Launch Web browser according to settings
     * @param c  Application context
     * @param url  URLfor start page
     */
    private void launchWeb(boolean external,Context c,String url) {

        if (external==false) {
            /**
             * Launch internal web browser as a WebView
             */
            if (url != null) {
                Intent intent = new Intent(c, WebActivity.class);
                // Clear activity stack so that back button on browser
                // view will cause app exit instead of return to login.
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.putExtra("webUrl", url); // Pass URL to browser activity
                startActivity(intent);
                // Finish login activity, back button in browser view will end app
                finish();
            } else
                Log.e(LTAG, "Error-launchWeb() called with null URL !");
        }
        else {
            /**
             * Launch external web browser
             */
 //         setImmersive();
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);
            // finish() on the activity means back button press in
            // browser view will not take us back to the login screen
            // but app will end.
            finish();
        }
    }

    /**
     * Launch Web browser activity with automatic browser selection
     * internal/external.  For devices with Android 5 or above
     * the internal webview is used (which is based on Chrome).
     * Otherwise an external browser is launched. If user has multiple
     * browsers installed the one launched depends on a setting external
     * to this app. Ln4 Solutions promotes use of the Chrome browser
     * as external browser and checks if it is installed. If not it
     * tried to assist the user in installing Chrome.
     * @param c  Application context
     * @param url  URLfor start page
     */
    private void launchWebAuto (Context c, String url) {
      if (Build.VERSION.SDK_INT >= 21) {  // If >= Android 5
            /**
             * Launch internal web browser as a WebView
             */
            if (url != null) {
                Intent intent = new Intent(c, WebActivity.class);
                // Clear activity stack so that back button on browser
                // view will cause app exit instead of return to login.
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.putExtra("webUrl", url); // Pass URL to browser activity
                startActivity(intent);
                // Finish login activity, back button in browser view will end app
                finish();
            } else
                Log.e(LTAG, "Error-launchWeb() called with null URL !");
      }
      else {
          /**
           *  We need Chrome technology - Launch external web browser
           */
          if (!chromeInstalled()) {
              if (chromeAssist()) // If user accepts installing Chrome
                // Redirect user to Chrome installation URL
                url = "https://play.google.com/store/apps/details?id=com.android.chrome";
          }
          Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
          startActivity(browserIntent);
          // finish() on the activity means back button press in
          // browser view will not take us back to the login screen
          // but this app will end.
          finish();  // Finish this activity
      }
    }

    public boolean chromeAssist() {
        /**
         * Prompt user to check if installing Chrome of interest
         */
        // TODO Implement prompting user about Chrome installation
        return false;
    }
/*
    public void setImmersive() {
        // Experimental: Test of setting parameters that
        // tells external browser to operate in "Immersive mode".
        // TODO: Currently disabled, has proven not to work reliably.
        // BEGIN_INCLUDE (get_current_ui_flags)
        // The UI options currently enabled are represented by a bitfield.
        // getSystemUiVisibility() gives us that bitfield.

        int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
        int newUiOptions = uiOptions;
        // END_INCLUDE (get_current_ui_flags)
        // BEGIN_INCLUDE (toggle_ui_flags)

        boolean isImmersiveModeEnabled =
                ((uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == uiOptions);
        if (isImmersiveModeEnabled) {
            Log.i(LTAG, "Turning immersive mode mode off. ");
        } else {
            Log.i(LTAG, "Turning immersive mode mode on.");
        }

        // Navigation bar hiding:  Backwards compatible to ICS.
        if (Build.VERSION.SDK_INT >= 14) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }

        // Status bar hiding: Backwards compatible to Jellybean
        if (Build.VERSION.SDK_INT >= 16) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;

        }

        // Immersive mode: Backward compatible to KitKat.
        // Note that this flag doesn't do anything by itself, it only augments the behavior
        // of HIDE_NAVIGATION and FLAG_FULLSCREEN.  For the purposes of this sample
        // all three flags are being toggled together.
        // Note that there are two immersive mode UI flags, one of which is referred to as "sticky".
        // Sticky immersive mode differs in that it makes the navigation and status bars
        // semi-transparent, and the UI flag does not get cleared when the user interacts with
        // the screen.
        if (Build.VERSION.SDK_INT >= 18) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }
        if (Build.VERSION.SDK_INT >= 11)
         getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
        //END_INCLUDE (set_ui_flags)
    }
*/

    private boolean chromeInstalled() {
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> applications = pm.getInstalledApplications(0);
        for (ApplicationInfo appInfo : applications) {
            if (appInfo.processName.contains("com.android.chrome"))
                return true;
        }
        return false;
    }
}
