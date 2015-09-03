package com.mobimation.ln4solutions;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
        String u=pref.getString("url",null);
        if (u!=null) {
            launchWeb(getApplicationContext(), u);
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

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
            if (webUrl!=null) {
                // Store granted URL
                SharedPreferences pref = context.getSharedPreferences(
                        "com.mobimation.URL", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("url", webUrl);
                editor.commit();
                // Launch WebActivity
                Log.d(TAG,"Launching web activity");
                status.setText("Loading browser..");
                launchWeb(context,webUrl);
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
    private void launchWeb(Context c,String url) {
        if (url !=null) {
            Intent intent = new Intent(c, WebActivity.class);
            // Clear activity stack so that back button on browser
            // view will cause app exit instead of return to login.
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.putExtra("webUrl", url); // Pass URL to browser activity
            startActivity(intent);
            finish();
        }
        else
            Log.e(LTAG,"Error-launchWeb() called with null URL !");
    }
}
