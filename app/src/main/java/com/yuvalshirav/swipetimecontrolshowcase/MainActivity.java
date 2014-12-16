package com.yuvalshirav.swipetimecontrolshowcase;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.TextAppearanceSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity {

    private static final int RESULT_TIME = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button setAlarmButton = (Button) findViewById(R.id.set_alarm);
        setAlarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TimeSelectorActivity.class);
                startActivityForResult(intent, RESULT_TIME);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null) {
            return;
        }

        Button setAlarmButton = (Button) findViewById(R.id.set_alarm);
        String buttonCallToAction = getResources().getString(R.string.set_alarm);
        String time = data.getStringExtra(TimeSelectorActivity.RESULT_TIME);
        if (time != null) {
            String rawButtonText = buttonCallToAction + "\n" + time;
            SpannableString buttonText = new SpannableString(rawButtonText);
            buttonText.setSpan(new TextAppearanceSpan(this, R.style.buttonTimeStyle), buttonCallToAction.length() + 1, rawButtonText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            setAlarmButton.setText(buttonText, TextView.BufferType.SPANNABLE);
        } else {
            setAlarmButton.setText(buttonCallToAction);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
}
