package com.mobimation.ln4solutions;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        final SharedPreferences pref = getApplicationContext().getSharedPreferences(
                "com.mobimation.URL", Context.MODE_PRIVATE);
        final CheckBox browserChoice = (CheckBox) findViewById(R.id.browserCheckbox);
        // Update checkbox with browser setting if any, else internal as default
        browserChoice.setChecked(pref.getBoolean("external",false));
        Button settingsDoneButton = (Button) findViewById(R.id.settingsDoneButton);
        settingsDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Store current browser preference
                SharedPreferences.Editor editor = pref.edit();
                editor.putBoolean("external",browserChoice.isChecked());
                editor.commit();
                // Close activity
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
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
}
