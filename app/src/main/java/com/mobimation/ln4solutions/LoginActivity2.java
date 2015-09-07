package com.mobimation.ln4solutions;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

/**
 * Experimental version of LoginActivity that makes use of the
 * custom formatting CustomerId widget.
 */
public class LoginActivity2 extends Activity {
    protected Button buttonSubmit;
    protected TextView status;
    private final String LTAG = LoginActivity2.class.getSimpleName();
    private static String url="https://h01.ln4solutions.com/code2url/code2url.php";
    private String webUrl;
    private String befo;
    private int count=0;
    private int aft;
    private int bef;
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
        // Setup UI components
        buttonSubmit = (Button)findViewById(R.id.buttonSubmit);
        status = (TextView)findViewById(R.id.status);
        status.setText(" ");
        final CustomerId customerId = (CustomerId)findViewById(R.id.customerId2);

        // Get granted URL if any and if so launch browser directly
        final SharedPreferences pref = this.getSharedPreferences(
                "com.mobimation.URL", Context.MODE_PRIVATE);
        // Use browser preference setting if any (internal=default)
        boolean external=pref.getBoolean("external", false);
        String u=pref.getString("url",null);
        if (u!=null) {
            launchWeb(external,getApplicationContext(), u);
            finish(); // Abort Login activity launch
        }
        else {
            customerId.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    aft=after;
                       befo=s.toString();
                    Log.d(LTAG,"Before="+"["+s+"]"+" start="+start+" count="+count+" after="+after);
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                   Log.d(LTAG,"On="+"["+s+"]"+" start="+start+" before="+before+" count="+count);
                    bef=before;
                }

                @Override
                public void afterTextChanged(Editable s) {
                    Log.d(LTAG,"After="+"["+s.toString()+"]");
                    if ((s.length()==3) || (s.length()==7)) {
                        if (befo.length()<=s.length())
                            customerId.setText(customerId.getText() + " ");
                        customerId.setSelection(customerId.length());
                    }
                    if (s.length()>11)
                        s.delete(s.length() - 1, s.length());

                    if (s.length()==11) {// Take VK down
                            VKdown();
                    }
                }
            });
            
            buttonSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String id = customerId.getText().toString();
                    id=id.replace(" ","");  // Clean up
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
     * Run asynchronous request for customer page URL.
     * Returns url if submitted id is valid, else null.
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
            return webUrl;
        }
        @Override
        protected void onProgressUpdate(String... values) {
            status.setText(values[0]);
        }

        @Override
        protected void onPostExecute(String webUrl) {
            if (webUrl!=null) {  // If web access granted
                // Store granted URL
                SharedPreferences pref = context.getSharedPreferences(
                        "com.mobimation.URL", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("url", webUrl);
                editor.commit();
                // Use browser preference setting if any (internal=default)
                boolean external=pref.getBoolean("external",false);
                // Launch WebActivity
                Log.d(TAG,"Launching web activity");
                status.setText("Loading browser..");
                launchWeb(external,context,webUrl);
                /* Intent intent = new Intent(context, WebActivity.class);
                intent.putExtra("webUrl", webUrl);
                startActivity(intent); */
            }
            else
                status.setText("Customer id "+code+" not valid");
        }
    }

    /**
     * Launch Web browser activity
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
        }
    }


    public void setImmersive() {

        // BEGIN_INCLUDE (get_current_ui_flags)
        // The UI options currently enabled are represented by a bitfield.
        // getSystemUiVisibility() gives us that bitfield.
        int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
        int newUiOptions = uiOptions;
        // END_INCLUDE (get_current_ui_flags)
        // BEGIN_INCLUDE (toggle_ui_flags)
        /*
        boolean isImmersiveModeEnabled =
                ((uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == uiOptions);
        if (isImmersiveModeEnabled) {
            Log.i(LTAG, "Turning immersive mode mode off. ");
        } else {
            Log.i(LTAG, "Turning immersive mode mode on.");
        }
*/
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
}
