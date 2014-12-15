package com.yuvalshirav.swipetimecontrol;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
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

    private float mStartX;
    private float mStartY;
    private float mX;
    private float mY;
    private float mLastX = Float.MIN_VALUE;
    private float mLastY = Float.MIN_VALUE;
    private Paint mTextPaint;
    private Paint mLinePaint;
    private Paint mFramePaint;
    private int mCanvasHeight;
    private int mCanvasWidth;
    private float mAttrActionBarHeight;
    private int mAttrTimeColor;
    private int mAttrBackgroundColor;
    private int mCursorRadius;
    private DrawThread mDrawThread;

    private boolean mRunning;

    private final static int SEGMENTS = 24;
    private final static int TOP_HOUR = 6;

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
            mAttrActionBarHeight = ta.getDimension(R.styleable.SwipeTimeView_action_bar_height, getResources().getDimension(R.dimen.action_bar_height));
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

        mLinePaint = new Paint();
        mLinePaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()));
        mLinePaint.setColor(Color.RED); // TODO: -----

        mFramePaint = new Paint();
        mFramePaint.setStrokeWidth(1);
        mFramePaint.setColor(Color.RED); // TODO: -----

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
        mY = Math.max(mAttrActionBarHeight + mCursorRadius, mY);
        mY = Math.min(mCanvasHeight - mCursorRadius, mY);
    }

    public void setStartPoint(float x, float y) {
        mStartX = x;
        mStartY = y;
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

            if (mStartX == 0) {
                setStartPoint(getWidth() / 2 - mCursorRadius, getHeight() / 2 - mCursorRadius);
            }

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
        return (int)Math.floor((mY - mAttrActionBarHeight) / ((mCanvasHeight - mAttrActionBarHeight) / SEGMENTS));
    }

    private Time getTime() {
        int segment = getSegment();
        int hour = segment + TOP_HOUR <= 23 ? segment + TOP_HOUR : segment + TOP_HOUR - 24;

        int xRange = mCanvasWidth - 2 * mCursorRadius;
        int minutes = 5 * Math.round(11 * mX / xRange);
        
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


        public DrawThread(SurfaceHolder surfaceHolder, Context context,
                            Handler handler) {
            mSurfaceHolder = surfaceHolder;
            mHandler = handler;
            mContext = context;

            mSegmentPaint = new Paint();
            mSegmentPaint.setColor(Color.BLACK);
        }
        public void doStart() {
            synchronized (mSurfaceHolder) {
                // TODO: start draw?

            }
        }
        public void run() {
            while (mRunning) {
                Canvas canvas = null;
                try {
                    canvas = mSurfaceHolder.lockCanvas(null);

                    if (canvas != null) {

                        if (mCanvasWidth == 0) {
                            mCanvasWidth = canvas.getWidth();
                            mCanvasHeight = canvas.getHeight();
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
            drawTime(canvas);
            canvas.drawCircle(mX, mY, mCursorRadius, mTextPaint);
        }

        private void drawTime(Canvas canvas) {
            Time time = getTime();
            String timeString = time.format("%H:%M");
            Rect textBounds = new Rect();
            mTextPaint.getTextBounds(timeString, 0, timeString.length(), textBounds);
            canvas.drawText(timeString, mCanvasWidth / 2, mAttrActionBarHeight / 2, mTextPaint);
        }

        private void drawBackground(Canvas canvas) {

            int segmentHeight = Math.round((mCanvasHeight - mAttrActionBarHeight) / SEGMENTS);

            // TODO: move paint
            for (int i=0; i<SEGMENTS; i++) {
                canvas.drawLine(0, i*segmentHeight + mAttrActionBarHeight, mCanvasWidth, i*segmentHeight + mAttrActionBarHeight, mSegmentPaint);
            }

        }
    }

}
