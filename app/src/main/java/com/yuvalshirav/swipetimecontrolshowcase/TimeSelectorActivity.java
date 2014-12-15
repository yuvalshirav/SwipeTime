package com.yuvalshirav.swipetimecontrolshowcase;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.format.Time;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.yuvalshirav.swipetimecontrol.SwipeTimeSurfaceView;


public class TimeSelectorActivity extends ActionBarActivity implements SwipeTimeSurfaceView.OnTimeChanged {

    public final static String RESULT_TIME = "result_time";
    public final static String RESULT_STATUS = "result_status";

    private TextView mTitleView;
    private TextView mCancelView;
    private Time mTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_selector);
        mTitleView = (TextView) findViewById(R.id.title);
        mCancelView = (TextView) findViewById(R.id.cancel);
    }


    @Override
    public void onTimeChanged(final Time time, final SwipeTimeSurfaceView.STATUS status, final SwipeTimeSurfaceView.DAY_PART dayPart) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mTitleView != null && status != SwipeTimeSurfaceView.STATUS.START) {
                    if (status == SwipeTimeSurfaceView.STATUS.DONE) {
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra(RESULT_TIME, mTime == null ? null : mTime.format("%H:%M")); // TODO: pass in serializable format
                        setResult(Activity.RESULT_OK, resultIntent);
                        finish();
                    } else if (status == SwipeTimeSurfaceView.STATUS.CANCEL) {
                        mTitleView.setVisibility(View.GONE);
                        mCancelView.setVisibility(View.VISIBLE);
                        mTime = null;
                    } else {
                        if (status == SwipeTimeSurfaceView.STATUS.MOVE) {
                            mTitleView.setText(time.format("%H:%M"));
                            mTime = time;
                        } else {
                            mTitleView.setText(dayPart.getStart() + ":00 - " + (dayPart.getEnd()+1) + ":00");
                            mTime = null;
                        }
                        mTitleView.setVisibility(View.VISIBLE);
                        mCancelView.setVisibility(View.GONE);
                    }
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_time_selector, menu);
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
