package com.mobimation.ln4solutions;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class UpdateActivity extends AppCompatActivity {

    Button button;
    TextView status;
    static String apk="ln4-alpha9.apk";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);
        button= (Button)findViewById(R.id.buttonCheck);
        status= (TextView)findViewById(R.id.updateStatus);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "http://laidback.tv/test/ln4/"+apk;
                String PATH = Environment.getExternalStorageDirectory() + "/download/";
                // Download APK and install it
                new Fetcher(getApplicationContext()).execute(url, PATH);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_update, menu);
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
     * Download file from server to local storage
     * @param url
     * @param outputFile
     */
    private static void downloadFile(String url, File outputFile) {
     /* // Alternative download code
        try {
            URL u = new URL(url);
            InputStream is = u.openStream();

            DataInputStream dis = new DataInputStream(is);

            byte[] buffer = new byte[1024];
            int length;

            FileOutputStream fos = new FileOutputStream( outputFile);
            while ((length = dis.read(buffer))>0) {
                fos.write(buffer, 0, length);
            }

        } catch (MalformedURLException mue) {
            Log.e("SYNC getUpdate", "malformed url error", mue);
        } catch (IOException ioe) {
            Log.e("SYNC getUpdate", "io error", ioe);
        } catch (SecurityException se) {
            Log.e("SYNC getUpdate", "security error", se);
        }

       */

        try {
            URL u = new URL(url);
            URLConnection conn = u.openConnection();
            int contentLength = conn.getContentLength();

            DataInputStream stream = new DataInputStream(u.openStream());

            byte[] buffer = new byte[contentLength];
            stream.readFully(buffer);
            stream.close();

            DataOutputStream fos = new DataOutputStream(new FileOutputStream(outputFile));
            fos.write(buffer);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            Log.e("FileNotFoundException", e + "");
            return;
        } catch (IOException e) {
            Log.e("IOException",e+"");
            return;
        }
    }

    /**
     * Install app from local storage
     * @param mycontext
     */
    private static void installApp(Context mycontext) {
        Intent installer = new Intent();
        installer.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        installer.setAction(android.content.Intent.ACTION_VIEW);
        String PATH = "file://" + Environment.getExternalStorageDirectory() + "/download/"+apk;
        installer.setDataAndType(Uri.parse(PATH), "application/vnd.android.package-archive");
        mycontext.startActivity(installer);
    }

    /**
     * Run asynchronous request for customer page URL.
     * Returns url if submitted id is valid, else null.
     */
    protected class Fetcher extends AsyncTask<String, String, String> {

        private final String TAG = Fetcher.class.getSimpleName();
        private Context context;
        private String code;

        protected Fetcher(Context context) {
            this.context = context.getApplicationContext();
        }

        @Override
        protected String doInBackground(String... params) {
            Log.d(TAG, "doInBackground");
            File f = new File(params[1]);
            f.mkdirs();
            File outputFile = new File(f, apk);
            downloadFile(params[0], outputFile);
            return "ok";
        }

        @Override
        protected void onPostExecute(String result) {
          Log.d(TAG, "onPostExcute");
            installApp(context);
        }
    }
}
