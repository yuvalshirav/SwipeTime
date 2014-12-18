package com.yuvalshirav.swipetimecontrolshowcase;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.format.Time;
import android.text.style.TextAppearanceSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.yuvalshirav.swipetimecontrol.SwipeTimeSurfaceView;


public class MainActivity extends ActionBarActivity implements SwipeTimeSurfaceView.OnTimeChanged {

    private static final int RESULT_TIME = 1;
    private SwipeTimeSurfaceView mTimeSelector;
    private ViewGroup mTimeSelectorWrapper;
    private View mEditBox;
    private TextView mTimeDisplay;
    private View mCancelDisplay;
    private Time mTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTimeSelectorWrapper = (ViewGroup) findViewById(R.id.time_selector_wrapper);
        mEditBox = findViewById(R.id.editbox);
        mTimeDisplay = (TextView) findViewById(R.id.title);
        mCancelDisplay = findViewById(R.id.cancel);

        final View mainInterface = findViewById(R.id.main_interface);

        Button setAlarmButton = (Button) findViewById(R.id.set_alarm);
        setAlarmButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    openTimeSelector();
                    mTimeSelectorWrapper.setVisibility(View.VISIBLE);
                    mainInterface.setVisibility(View.GONE);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    closeTimeSelector();
                    mTimeSelectorWrapper.setVisibility(View.INVISIBLE);
                    mainInterface.setVisibility(View.VISIBLE);
                }

                if (mTimeSelector != null && mTimeSelector.isRunning()) {
                    int location[] = new int[4];
                    mTimeSelectorWrapper.getLocationOnScreen(location); // TODO: cache this
                    // offset touch location
                    mTimeSelector.onTouchEvent(event, event.getRawX(), event.getRawY() - location[1]);

                }
                return true;
            }
        });

    }

    private void openTimeSelector() {
        hideSoftKeyboard();
        LayoutInflater inflater = getLayoutInflater();
        FrameLayout wrapper = (FrameLayout) inflater.inflate(R.layout.time_selector, mTimeSelectorWrapper);
        mTimeSelector = (SwipeTimeSurfaceView) ((ViewGroup)wrapper.getChildAt(0)).getChildAt(0); // Can't get the SurfaceView via it's id + it's auto-wrapper by a FrameLayout
        mEditBox.setVisibility(View.GONE);
    }

    private void closeTimeSelector() {
        if (mTimeSelector != null) {
            mTimeSelector.surfaceDestroyed(mTimeSelector.getHolder());
            mTimeSelectorWrapper.removeView((View)mTimeSelector.getParent());
        }
        mEditBox.setVisibility(View.VISIBLE);
        mTimeDisplay.setVisibility(View.GONE);
        mCancelDisplay.setVisibility(View.GONE);
        mTimeSelector = null;
        displaySelectedTime();
    }

    @Override
    public void onTimeChanged(final Time time, final SwipeTimeSurfaceView.STATUS status, final SwipeTimeSurfaceView.DAY_PART dayPart) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (status == SwipeTimeSurfaceView.STATUS.CANCEL && mTimeSelector != null) {
                    mCancelDisplay.setVisibility(View.VISIBLE);
                    mTimeDisplay.setVisibility(View.GONE);
                    mTime = null;
                } else if (mTimeSelector != null) {
                    if (status == SwipeTimeSurfaceView.STATUS.MOVE) {
                        mTimeDisplay.setText(time.format("%H:%M"));
                        mTime = time;
                    } else if (status != SwipeTimeSurfaceView.STATUS.START) {
                        mTimeDisplay.setText(dayPart.getStart() + ":00 - " + (dayPart.getEnd() + 1) + ":00");
                        mTime = null;
                    }
                    mTimeDisplay.setVisibility(View.VISIBLE);
                    mCancelDisplay.setVisibility(View.GONE);
                }
            }
        });

    }


    private void displaySelectedTime() {
        Button setAlarmButton = (Button) findViewById(R.id.set_alarm);
        String buttonCallToAction = getResources().getString(R.string.set_alarm);
        if (mTime != null) {
            String rawButtonText = buttonCallToAction + "\n" + mTime.format("%H:%M");
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

    private void hideSoftKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
}
