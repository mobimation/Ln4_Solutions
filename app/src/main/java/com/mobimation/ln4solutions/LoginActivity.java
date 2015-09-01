package com.mobimation.ln4solutions;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends Activity {
    protected Button buttonSubmit;
    protected TextView status;
    private static String url="https://h01.ln4solutions.com/code2url/code2url.php";
    private String webUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Setup UI components
        buttonSubmit = (Button)findViewById(R.id.buttonSubmit);
        status = (TextView)findViewById(R.id.status);
        status.setText(" ");
        final EditText customerId = (EditText)findViewById(R.id.customerId);

        // Assemble and format customer id based on key input
        customerId.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return false;
            }
        });
        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = customerId.getText().toString();
                new Authenticator(getApplicationContext()).execute(url, id);
            }
        });
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Run asynchronous request for customer page URL.
     * Returns url if submitted id is valid, else null.
     */
    protected class Authenticator extends AsyncTask<String, String, String> {

        private final String TAG = Authenticator.class.getSimpleName();

        Context context;

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
            this.context = context.getApplicationContext();
        }

        @Override
        protected String doInBackground(String... params) {
            Log.d(TAG, "doInBackground");
            String webUrl=null;
            publishProgress("Submitting..");

           /**
            * Submit JSON request over https to Ln4 Solutions server
            */
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(params[0]);
            try {
                JSONObject json = new JSONObject();
                json.put("code", params[1]);
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
                    Log.d(TAG,"Valid customer, URL="+webUrl);
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
                // Launch WebActivity
                Log.d(TAG,"Launching web activity");
                status.setText("Loading browser..");
                Intent intent = new Intent(context, WebActivity.class);
                intent.putExtra("webUrl", webUrl);
                startActivity(intent);
            }
            else
                status.setText("Customer id not valid");
        }
    }
}
