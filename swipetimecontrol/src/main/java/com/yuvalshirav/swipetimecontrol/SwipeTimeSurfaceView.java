package com.yuvalshirav.swipetimecontrol;

import android.content.Context;
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

    private boolean mRunning;

    private final static int SEGMENTS = 12;
    private final static int TOP_HOUR = 6;

    private enum STATUS {
        START, MOVE, DAY, NIGHT, CANCEL
    }
    private STATUS mStatus = STATUS.START;

    private enum DAY_PART {
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
    private DAY_PART dayPart = DAY_PART.DAYTIME;

    SwipeTimeSurfaceView(Context context, AttributeSet attrs) {
        super(context);

        setupAttrs(context, attrs);

        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);

        // TODO: move (xml?)
        //setBackgroundColor(Color.BLACK);

        //setZOrderOnTop(true);    // necessary
        //mSurfaceHolder.setFormat(PixelFormat.OPAQUE);
        setZOrderOnTop(true);
        mSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        setBackgroundColor(mAttrBackgroundColor);

        setupPaints();

    }

    private void setupAttrs(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SwipeTimeView, 0, 0);
        try {
            mAttrBottomToolbarHeight = mAttrActionBarHeight = ta.getDimension(R.styleable.SwipeTimeView_action_bar_height, getResources().getDimension(R.dimen.action_bar_height));
            mAttrTimeColor = ta.getColor(R.styleable.SwipeTimeView_time_color, getResources().getColor(R.color.time));
            mAttrBackgroundColor = ta.getColor(R.styleable.SwipeTimeView_background_color, getResources().getColor(R.color.background));
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
            dayPart = DAY_PART.DAYTIME;
        } else if (mNighttimeBounds.contains(mX, mY)) {
            mStatus = STATUS.NIGHT;
            dayPart = DAY_PART.NIGHTIME;
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
                break;
        }
        return handle;
    }

    // TODO: !!!!!! is the thread automaticaly handled?
    protected void refresh() {
        if(mRunning && mSurfaceHolder.getSurface().isValid()){
            Canvas canvas = mSurfaceHolder.lockCanvas();


            /*
            canvas.drawColor( 0, PorterDuff.Mode.CLEAR );
            int xPos = (canvas.getWidth() / 2);
            int yPos = (int) ((canvas.getHeight() / 2) - ((mTextPaint.descent() + mTextPaint.ascent()) / 2)) ;
            canvas.drawText("(" + mX + ", " + mY + ")", xPos, yPos, mTextPaint);
            */

            canvas.drawColor(Color.WHITE, PorterDuff.Mode.CLEAR);
            //canvas.drawLine(mStartX, mStartY, mX, mY, mLinePaint);

            // TODO: remove!
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setFilterBitmap(true);
            paint.setDither(true);

            //canvas.drawBitmap(mCursorBitmap, mX + mCursorBitmap.getScaledWidth(canvas) / 2, mY - mCursorBitmap.getScaledHeight(canvas) / 2, mFramePaint);

            //refreshTargets(canvas);
            mSurfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    protected void refreshTargets(Canvas canvas) {
        double totalRad = Math.PI * 0.35;
        double startRad = Math.PI * 0.1;
        int nPoints = 5;
        double pointRad = totalRad / nPoints;
        float r = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 350, getResources().getDisplayMetrics());
        float a = 0;
        float b = 0;

        // x = a + r cos t
        // y = b + r sin t
        for (int i=0; i<nPoints; i++) {
            float x = (float)(a + r * Math.cos(startRad + pointRad * i));
            float y = (float)(b + r * Math.sin(startRad + pointRad * i));
            String label = "point " + i;
            canvas.drawText(label, x, y, mTextPaint);

            Rect textBounds = new Rect();
            mTextPaint.getTextBounds(label, 0, label.length(), textBounds);
            float labelHeight = mTextPaint.ascent() + mTextPaint.descent();
            float labelWidth = mTextPaint.measureText(label);
            //canvas.drawRect(x - labelWidth, y, x, y + labelHeight, mFramePaint);
            //canvas.drawRect(textBounds.left + x - labelWidth, textBounds.top + y, textBounds.right + x - labelWidth, textBounds.bottom + y, mFramePaint);
            textBounds.offset((int)(x - labelWidth), (int)y);
            canvas.drawRect(textBounds.left, textBounds.top, textBounds.right, textBounds.bottom, mFramePaint);
        }

    }

    protected void refreshTargetsArc(Canvas canvas) {

    }

    private int getSegment() {
        int segment = (int)Math.floor((mY - mAttrActionBarHeight) / ((mCanvasHeight - mAttrActionBarHeight - mAttrBottomToolbarHeight) / SEGMENTS));
        return mY > mAttrActionBarHeight && segment < SEGMENTS ? segment : -1;
    }

    private Time getTime() {
        int segment = getSegment();

        // inside toolbar
        if (mStatus == STATUS.NIGHT || mStatus == STATUS.DAY) {
            return null;
        }

        int hour = segment + dayPart.getStart() <= 23 ? segment + dayPart.getStart() : segment + dayPart.getStart() - 24;
        int xRange = mCanvasWidth - 2 * mCursorRadius;
        int minutes = 5 * Math.round(11 * (mX - mCursorRadius) / xRange);

        Time time = new Time();
        time.setToNow();
        time.set(0, minutes, hour, time.monthDay, time.month, time.year);

        return time;
    }

    public void onResume() {

    }

    public void onPause() {

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
                drawOnboarding(canvas);
            } else {
                if (mStatus == STATUS.CANCEL) {
                    drawCancel(canvas);
                } else {
                    drawTime(canvas);
                    if (mStatus == STATUS.MOVE) {
                        canvas.drawCircle(mX, mY, mCursorRadius, mTextPaint);
                    }
                }
            }
        }

        private void drawTime(Canvas canvas) {
            Time time = getTime();
            String timeString = time != null ? time.format("%H:%M") : getResources().getString(dayPart.getTitleRes());
            Rect textBounds = new Rect();
            mTextPaint.getTextBounds(timeString, 0, timeString.length(), textBounds);
            canvas.drawText(timeString, mCanvasWidth / 2, (mAttrActionBarHeight + textBounds.height()) / 2, mTextPaint);
        }

        private void drawBackground(Canvas canvas) {

            int segmentHeight = Math.round((mCanvasHeight - mAttrActionBarHeight - mAttrBottomToolbarHeight) / SEGMENTS);

            // TODO: move paint
            for (int i=0; i<SEGMENTS; i++) {
                canvas.drawLine(0, i*segmentHeight + mAttrActionBarHeight, mCanvasWidth, i*segmentHeight + mAttrActionBarHeight, mSegmentPaint);
            }


        }

        private void drawToggles(Canvas canvas) {
            if (mDaytimePath == null) {
                setupToggles(); // TODO: not here, maybe delegate to a thread
            }

            canvas.drawPath(mDaytimePath, mFramePaint);
            if (dayPart == DAY_PART.DAYTIME) {
                canvas.drawRect(mDaytimeBounds, mToggleActivePaint);
            }

            canvas.drawPath(mNighttimePath, mFramePaint);
            if (dayPart == DAY_PART.NIGHTIME) {
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
