package com.yuvalshirav.swipetimecontrol;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Handler;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by yuvalshirav on 12/14/14.
 */
public class SwipeTimeSurfaceView extends SurfaceView implements Runnable, SurfaceHolder.Callback {

    public interface OnTimeChanged {

        void onTimeChanged(Time time, STATUS status, DAY_PART dayPart);

    }

    private OnTimeChanged mOnTimeChanged;
    private SurfaceHolder mSurfaceHolder;
    private Thread mRenderThread = null;

    private float mX;
    private float mY;
    private float mLastX = Float.MIN_VALUE;
    private float mLastY = Float.MIN_VALUE;
    private Paint mTextPaint;
    private Paint mOnboardingPaint;
    private Paint mLinePaint;
    private Paint mFramePaint;
    private Paint mTagPaint;
    private Paint mTagMainPaint;
    private Paint mToggleTextPaint;
    private Paint mToggleActivePaint;
    private Paint mCancelPaintText;
    private Paint mCancelPaint;
    private int mCanvasHeight;
    private int mCanvasWidth;
    private float mAttrActionBarHeight;
    private float mAttrBottomToolbarHeight;
    private int mAttrTimeColor;
    private int mAttrBackgroundColor;
    private int mCursorRadius;
    private RectF mCancelBounds = new RectF();
    private RectF mDaytimeBounds = new RectF();
    private RectF mNighttimeBounds = new RectF();
    private DrawThread mDrawThread;
    private Time mLastTime = new Time();

    private boolean mRunning;

    private final static int SEGMENTS = 12;
    private final static int TOP_HOUR = 6;

    public enum STATUS {
        START, MOVE, DAY, NIGHT, CANCEL, DONE
    }
    private STATUS mStatus = STATUS.START;

    public enum DAY_PART {
        DAYTIME(6, 17, R.string.daytime), NIGHTIME(18, 5, R.string.nighttime);

        private int start;
        private int end;
        private int titleRes;

        DAY_PART(int start, int end, int titleRes) {
            this.start = start;
            this.end = end;
            this.titleRes = titleRes;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        public int getTitleRes() {
            return titleRes;
        }
    }
    private DAY_PART mDayPart = DAY_PART.DAYTIME;

    public SwipeTimeSurfaceView(Context context, AttributeSet attrs) {
        super(context);

        setupAttrs(context, attrs);

        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);

        setZOrderOnTop(true);
        mSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        setBackgroundColor(mAttrBackgroundColor);

        setupPaints();

        mOnTimeChanged = (OnTimeChanged)context;

    }

    private void setupAttrs(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SwipeTimeSurfaceView, 0, 0);
        try {
            mAttrActionBarHeight = 0;
            mAttrBottomToolbarHeight =  ta.getDimension(R.styleable.SwipeTimeSurfaceView_action_bar_height, getResources().getDimension(R.dimen.action_bar_height));
            mAttrTimeColor = ta.getColor(R.styleable.SwipeTimeSurfaceView_time_color, getResources().getColor(R.color.time));
            mAttrBackgroundColor = ta.getColor(R.styleable.SwipeTimeSurfaceView_background_color, getResources().getColor(R.color.background));
        } finally {
            ta.recycle();
        }
    }

    void setupPaints() {
        mCursorRadius = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics()));

        mTextPaint = new Paint();
        mTextPaint.setColor(mAttrTimeColor);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mTextPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 24, getResources().getDisplayMetrics()));

        mTagPaint = new Paint();
        mTagPaint.setColor(getResources().getColor(R.color.time_tag)); // TODO: use attr
        mTagPaint.setTextAlign(Paint.Align.CENTER);
        mTagPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mTagPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14, getResources().getDisplayMetrics()));

        mTagMainPaint = new Paint();
        mTagMainPaint.setColor(getResources().getColor(R.color.time)); // TODO: use attr
        mTagMainPaint.setTextAlign(Paint.Align.CENTER);
        mTagMainPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mTagMainPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14, getResources().getDisplayMetrics()));


        mCancelPaint = new Paint();
        mCancelPaint.setColor(Color.RED); // TODO: use attr

        mCancelPaintText = new Paint();
        mCancelPaintText.setColor(Color.WHITE); // TODO: use attr
        mCancelPaintText.setTextAlign(Paint.Align.CENTER);
        mCancelPaintText.setTypeface(Typeface.DEFAULT_BOLD);
        mCancelPaintText.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 24, getResources().getDisplayMetrics()));

        mOnboardingPaint = new Paint();
        mOnboardingPaint.setColor(mAttrTimeColor);
        mOnboardingPaint.setTextAlign(Paint.Align.CENTER);
        mOnboardingPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mOnboardingPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 18, getResources().getDisplayMetrics()));

        mLinePaint = new Paint();
        mLinePaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()));

        mFramePaint = new Paint();
        mFramePaint.setStyle(Paint.Style.STROKE);
        mFramePaint.setStrokeWidth(1);
        mFramePaint.setColor(Color.BLACK); // TODO: use attr

        mToggleTextPaint = new Paint();
        mToggleTextPaint.setColor(Color.BLACK); // TODO: use attr
        mToggleTextPaint.setTextAlign(Paint.Align.LEFT);
        mToggleTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mToggleTextPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));

        mToggleActivePaint = new Paint();
        mToggleActivePaint.setColor(getResources().getColor(R.color.toggle_active_bg)); // TODO: use attr

    }

    @Override
    public void run() {
        while (mRunning) {
            // TODO: should use other pattern for invalidation?
            if (mLastX != mX || mLastY != mY) {
                //refresh();
                //mLastX = mX;
                //mLastY = mY;
            }
        }
    }

    /*
    public boolean onTouchEvent(MotionEvent event) {
        boolean handle = true;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                setX(event.getX());
                setY(event.getY());
                break;
        }
        return handle;
    }*/

    public float getX() {
        return mX;
    }

    public float getY() {
        return mY;
    }

    public void setPoint(float x, float y) {
        mX = x;
        mY = y;

        // boundaries
        mX = Math.min(mCanvasWidth - mCursorRadius, mX);
        mX = Math.max(mCursorRadius, mX);
        //mY = Math.max(mAttrActionBarHeight + mCursorRadius, mY);
        mY = Math.min(mCanvasHeight - mCursorRadius, mY);

        if (mDaytimeBounds.contains(mX, mY)) {
            mStatus = STATUS.DAY;
            mDayPart = DAY_PART.DAYTIME;
        } else if (mNighttimeBounds.contains(mX, mY)) {
            mStatus = STATUS.NIGHT;
            mDayPart = DAY_PART.NIGHTIME;
        } else if (mY <= mCancelBounds.bottom) {
            mStatus = STATUS.CANCEL;
        } else {
            mStatus = STATUS.MOVE;
        }

    }

    // TODO: use drag event?
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean handle = true;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                handle = true;
                break;
            case MotionEvent.ACTION_MOVE:
                handle = true;
                setPoint(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_UP:
                handle = true;
                if (mStatus == STATUS.MOVE || mStatus == STATUS.CANCEL) {
                    mStatus = STATUS.DONE;
                    if (mOnTimeChanged != null) {
                        mOnTimeChanged.onTimeChanged(getTime(), mStatus, mDayPart);
                    }
                }
                break;
        }
        return handle;
    }


    private int getSegment() {
        int segment = (int)Math.floor((mY - mAttrActionBarHeight) / ((mCanvasHeight - mAttrActionBarHeight - mAttrBottomToolbarHeight) / SEGMENTS));
        return mY > mAttrActionBarHeight && segment < SEGMENTS ? segment : -1;
    }

    public Time getTime() {
        // inside toolbar
        if (mStatus == STATUS.NIGHT || mStatus == STATUS.DAY || mStatus == STATUS.CANCEL) {
            return null;
        }

        return getTime(getSegment(), mX);
    }

    private Time getTime(int segment, float x) {

        int hour = segment + mDayPart.getStart() <= 23 ? segment + mDayPart.getStart() : segment + mDayPart.getStart() - 24;
        int xRange = mCanvasWidth - 2 * mCursorRadius;
        int minutes = 5 * Math.round(11 * (x - mCursorRadius) / xRange);

        Time time = new Time();
        time.setToNow();
        time.set(0, minutes, hour, time.monthDay, time.month, time.year);

        return time;
    }

    public void onResume() {
        // draw thread handled in SurfaceHolder callbacks
    }

    public void onPause() {
        // draw thread handled in SurfaceHolder callbacks
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mDrawThread = new DrawThread(mSurfaceHolder, getContext(), new Handler());
        mDrawThread.setRunning(true);
        mDrawThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mDrawThread != null) {
            mCanvasWidth = width;
            mCanvasHeight = height;
            mDrawThread.setSurfaceSize(width, height);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        if (mDrawThread != null) {
            mDrawThread.setRunning(false);
            while (retry) {
                try {
                    mDrawThread.join();
                    retry = false;
                } catch (InterruptedException e) {
                }
            }
        }
    }

    public void setOnTimeChanged(OnTimeChanged onTimeChanged) {
        this.mOnTimeChanged = onTimeChanged;
    }

    class DrawThread extends Thread {

        private boolean mRunning = false;
        private int mCanvasWidth;
        private int mCanvasHeight;

        private SurfaceHolder mSurfaceHolder;
        private Context mContext;
        private Handler mHandler;
        private Paint mSegmentPaint;
        private Bitmap mDaytimeBitmap;
        private Bitmap mNighttimeBitmap;
        private Path mDaytimePath;
        private Path mNighttimePath;

        public DrawThread(SurfaceHolder surfaceHolder, Context context,
                            Handler handler) {
            mSurfaceHolder = surfaceHolder;
            mHandler = handler;
            mContext = context;

            mSegmentPaint = new Paint();
            mSegmentPaint.setColor(Color.BLACK);
        }

        // TODO: need this?
        public void doStart() {
            synchronized (mSurfaceHolder) {
                // TODO: start draw?

            }
        }

        private void setupToggles() {
            // TODO: maybe place in yet another thread

            int scaledHeight = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, getResources().getDisplayMetrics()));
            int scaledWidth;

            mDaytimeBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.daytime);
            scaledWidth = Math.round(((float)scaledHeight / (float)mDaytimeBitmap.getHeight()) * mDaytimeBitmap.getWidth());
            mDaytimeBitmap = Bitmap.createScaledBitmap(mDaytimeBitmap, scaledWidth, scaledHeight, true);

            mNighttimeBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.nighttime);
            scaledWidth = Math.round(((float)scaledHeight / (float)mNighttimeBitmap.getHeight()) * mNighttimeBitmap.getWidth());
            mNighttimeBitmap = Bitmap.createScaledBitmap(mNighttimeBitmap, scaledWidth, scaledHeight, true);

            mDaytimePath = new Path();
            mDaytimePath.addRect(0, mCanvasHeight - mAttrBottomToolbarHeight, mCanvasWidth / 2, mCanvasHeight, Path.Direction.CW);
            mDaytimePath.computeBounds(mDaytimeBounds, true);
            mDaytimeBounds.inset(1, 1);
            mNighttimePath = new Path();
            mNighttimePath.addRect(mCanvasWidth / 2, mCanvasHeight - mAttrBottomToolbarHeight, mCanvasWidth, mCanvasHeight, Path.Direction.CW);
            mNighttimePath.computeBounds(mNighttimeBounds, true);
            mNighttimeBounds.inset(1, 1);

        }


        public void run() {
            while (mRunning) {
                Canvas canvas = null;
                try {
                    canvas = mSurfaceHolder.lockCanvas(null);

                    if (canvas != null) {

                        if (mStatus == STATUS.START) { // TODO
                            mCanvasWidth = canvas.getWidth();
                            mCanvasHeight = canvas.getHeight();
                            mCancelBounds = new RectF(0, 0, mCanvasWidth, mAttrActionBarHeight);
                        }

                        synchronized (mSurfaceHolder) {
                            draw(canvas);
                        }
                    }
                } finally {
                    if (canvas != null) {
                        mSurfaceHolder.unlockCanvasAndPost(canvas);
                    }
                }
            }
        }

        public void setRunning(boolean running) {
            mRunning = running;
        }
        public void setSurfaceSize(int width, int height) {
            synchronized (mSurfaceHolder) {
                mCanvasWidth = width;
                mCanvasHeight = height;
                doStart();
            }
        }
        private void draw(Canvas canvas) {
            //canvas.restore(); // TODO: save and restore?
            canvas.drawColor(Color.WHITE, PorterDuff.Mode.CLEAR);
            drawBackground(canvas);
            drawToggles(canvas);
            if (mStatus == STATUS.START) {
                //drawOnboarding(canvas);
            } else {
                if (mStatus == STATUS.CANCEL) {
                    //drawCancel(canvas);
                } else {
                    //drawTime(canvas);
                    if (mStatus == STATUS.MOVE) {
                        canvas.drawCircle(mX, mY, mCursorRadius, mTextPaint);
                    }
                }
            }

            // delegate time draw
            Time time = getTime();
            if (time == null || mLastTime == null || Time.compare(time, mLastTime) != 0) {
                if (mOnTimeChanged != null) {
                    mOnTimeChanged.onTimeChanged(time, mStatus, mDayPart);
                }
                mLastTime = time;
            }

        }

        private void drawTime(Canvas canvas) {
            Time time = getTime();
            String timeString = time != null ? time.format("%H:%M") : getResources().getString(mDayPart.getTitleRes());
            Rect textBounds = new Rect();
            mTextPaint.getTextBounds(timeString, 0, timeString.length(), textBounds);
            canvas.drawText(timeString, mCanvasWidth / 2, (mAttrActionBarHeight + textBounds.height()) / 2, mTextPaint);
        }

        private void drawBackground(Canvas canvas) {

            int segmentHeight = Math.round((mCanvasHeight - mAttrActionBarHeight - mAttrBottomToolbarHeight) / SEGMENTS);
            int xRange = mCanvasWidth - 2 * mCursorRadius;
            int minuteRange = xRange / 11;

            // TODO: move paint
            for (int i=0; i<SEGMENTS; i++) {
                canvas.drawLine(0, i*segmentHeight + mAttrActionBarHeight, mCanvasWidth, i*segmentHeight + mAttrActionBarHeight, mSegmentPaint);


                // draw time tags
                for (int j=0; j<=11; j++) {
                    int x = Math.round(minuteRange * (j + 0.5f));
                    Rect textBounds = null;
                    if (textBounds == null) {
                        textBounds = new Rect();
                        mTagPaint.getTextBounds("00", 0, 2, textBounds);
                    }

                    Time time = getTime(i, x);
                    String tag = (j == 0) ? time.format("%H") : time.format(":%M");
                    canvas.drawText(tag, x - 5, i*segmentHeight + mAttrActionBarHeight + (segmentHeight + textBounds.height()) / 2, j == 0 ? mTagMainPaint : mTagPaint);
                }
            }
        }

        private void drawToggles(Canvas canvas) {
            if (mDaytimePath == null) {
                setupToggles(); // TODO: not here, maybe delegate to a thread
            }

            canvas.drawPath(mDaytimePath, mFramePaint);
            if (mDayPart == DAY_PART.DAYTIME) {
                canvas.drawRect(mDaytimeBounds, mToggleActivePaint);
            }

            canvas.drawPath(mNighttimePath, mFramePaint);
            if (mDayPart == DAY_PART.NIGHTIME) {
                canvas.drawRect(mNighttimeBounds, mToggleActivePaint);
            }


            Rect daytimeTextBounds = new Rect(); // TODO: delegate upwards
            String daytimeTitle = getResources().getString(R.string.daytime);
            mToggleTextPaint.getTextBounds(daytimeTitle, 0, daytimeTitle.length(), daytimeTextBounds);
            float daytimeBitmapLeft = (mDaytimeBounds.width() - mDaytimeBitmap.getWidth() - daytimeTextBounds.width() * 1.3f) / 2;
            canvas.drawBitmap(mDaytimeBitmap, daytimeBitmapLeft, mCanvasHeight - (mDaytimeBounds.height() + mDaytimeBitmap.getHeight()) / 2, null);
            canvas.drawText(daytimeTitle, daytimeBitmapLeft + mDaytimeBitmap.getWidth(), mCanvasHeight - (mAttrBottomToolbarHeight - daytimeTextBounds.height()) / 2, mToggleTextPaint);

            Rect nighttimeTextBounds = new Rect(); // TODO: delegate upwards
            String nighttimeTitle = getResources().getString(R.string.nighttime);
            mToggleTextPaint.getTextBounds(daytimeTitle, 0, nighttimeTitle.length(), nighttimeTextBounds);
            float nighttimeBitmapLeft = (mCanvasWidth + mNighttimeBounds.width() - mNighttimeBitmap.getWidth() - nighttimeTextBounds.width() * 1.3f) / 2;
            canvas.drawBitmap(mNighttimeBitmap, nighttimeBitmapLeft, mCanvasHeight - (mNighttimeBounds.height() + mNighttimeBitmap.getHeight()) / 2, null);
            canvas.drawText(nighttimeTitle, nighttimeBitmapLeft + mNighttimeBitmap.getWidth(), mCanvasHeight - (mAttrBottomToolbarHeight - nighttimeTextBounds.height()) / 2, mToggleTextPaint);


        }

        private void drawOnboarding(Canvas canvas) {
            String onboardingText = getResources().getString(R.string.onboarding);
            Rect onboardingBounds = new Rect();
            mOnboardingPaint.getTextBounds(onboardingText, 0, onboardingText.length(), onboardingBounds);
            canvas.drawText(onboardingText, mCanvasWidth / 2, (mAttrActionBarHeight + onboardingBounds.height()) / 2, mOnboardingPaint);
        }

        private void drawCancel(Canvas canvas) {
            canvas.drawRect(mCancelBounds, mCancelPaint);

            String cancelText = getResources().getString(R.string.cancel);
            Rect textBounds = new Rect();
            mCancelPaintText.getTextBounds(cancelText, 0, cancelText.length(), textBounds);
            canvas.drawText(cancelText, mCanvasWidth / 2, (mAttrActionBarHeight + textBounds.height()) / 2, mCancelPaintText);

        }

    }

}
