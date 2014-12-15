package com.yuvalshirav.swipetimecontrol;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.SpinnerAdapter;

/**
 * Created by yuvalshirav on 12/14/14.
 */
public class SwipeTimeView extends FrameLayout {

    // TODO: need any of these?
    private String mExampleString; // TODO: use a default from R.string...
    private int mExampleColor = Color.RED; // TODO: use a default from R.color...
    private float mExampleDimension = 0; // TODO: use a default from R.dimen...
    private Drawable mExampleDrawable;

    private TextPaint mTextPaint;
    private float mTextWidth;
    private float mTextHeight;

    private int mSnapToId;
    private STATE mState;
    private float mX;
    private float mY;
    private SwipeTimeSurfaceView mSwipeTimeSurfaceView;
    private Dialog mWrapperDialog;
    private AttributeSet mAttributeSet;

    private enum STATE {
        BUTTON, START, MOVE, END
    }


    public SwipeTimeView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public SwipeTimeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public SwipeTimeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {

        mAttributeSet = attrs; // later to be passed on to SurfaceView

        // Set up a default TextPaint object
        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.LEFT);

        // Update TextPaint and text measurements from attributes
        invalidateTextPaintAndMeasurements();

        prep();

    }

    protected Dialog createWrapperDialog(View view) {
        Dialog dialog = new Dialog(getActivity() ,android.R.style.Theme_NoTitleBar);
        dialog.setContentView(view);
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height= WindowManager.LayoutParams.MATCH_PARENT;
        //lp.gravity=Gravity.BOTTOM | Gravity.LEFT;
        lp.dimAmount = 0;
        dialog.getWindow().setAttributes(lp);
        return dialog;

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean handle = true;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                handle = onDown(event);
                break;
            case MotionEvent.ACTION_MOVE:
                handle = onMove(event);
                break;
            case MotionEvent.ACTION_UP:
                //handle = onUp(event);
                break;
        }
        return handle;
    }

    private void prep() {

        mSwipeTimeSurfaceView = new SwipeTimeSurfaceView(getContext(), mAttributeSet);
        mSwipeTimeSurfaceView.setX(0);
        mSwipeTimeSurfaceView.setY(0);

        mWrapperDialog = createWrapperDialog(mSwipeTimeSurfaceView);
    }

    private boolean onDown(MotionEvent event) {

        if (mState != STATE.START) {

            // TODO: window leaked!
            mWrapperDialog.show();
            mSwipeTimeSurfaceView.onResume();

            //View rootView = getActivity().getWindow().getDecorView().findViewById(android.R.id.content);
            //getActivity().addContentView(mSwipeTimeSurfaceView, new ViewGroup.LayoutParams(rootView.getHeight(), ViewGroup.LayoutParams.MATCH_PARENT));

            //mSwipeTimeSurfaceView.setStartPoint(event.getX(), event.getY());

        }
        mState = STATE.START;
        return true;

    }

    private boolean onMove(MotionEvent event) {
        mState = STATE.MOVE;
        if (mSwipeTimeSurfaceView != null) {
            mSwipeTimeSurfaceView.setPoint(event.getX(), event.getY());
        }
        return true;
    }

    private boolean onUp(MotionEvent event) {
        mState = STATE.BUTTON;
        /*if (mSwipeTimeSurfaceView != null) {
            removeAddedView(mSwipeTimeSurfaceView);
            mSwipeTimeSurfaceView = null;
        }*/

        if (mWrapperDialog != null) {
            mSwipeTimeSurfaceView.onPause();
            mWrapperDialog.cancel();
            mWrapperDialog = null;
            mSwipeTimeSurfaceView = null; // TODO: probably better to reuse
            prep();
        }
        return true;
    }

    private void removeAddedView(View view) {
        ViewGroup rootView = (ViewGroup) findViewById(android.R.id.content);
        for (int i = 0; i < rootView.getChildCount(); i++) {
            if (rootView.getChildAt(i) == view) {
                rootView.removeViewAt(i);
            }
        }
    }

    private Activity getActivity() {
        return (Activity)getContext();
    }

    // TODO: best way to expand - probably handle touch events from button control, draw expansion via OverlayGroup. First question is, what happen to touch event started in view, when touch leaves said view
    // TODO: better idea - use a SurfaceView. Still, how do I expand said SurfaceView? Also, will the touch event first triggered on the button propagate to the surface view? How fast? On the other hand, the touch events can be triggered on the button (outside its bounds), and manually passed on to the SurfaceView



    private View getSnapToView() {
        View snapToView = getActivity().findViewById(mSnapToId);

        return getActivity().findViewById(mSnapToId);
    }

    private void invalidateTextPaintAndMeasurements() {
        /*
        mTextPaint.setTextSize(mExampleDimension);
        mTextPaint.setColor(mExampleColor);
        mTextWidth = mTextPaint.measureText(mExampleString);

        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        mTextHeight = fontMetrics.bottom;
        */
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(2);
        canvas.drawRect(0, 0, 150, 150, paint);

        // TODO: consider storing these as member variables to reduce
        // allocations per draw cycle.
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        int contentWidth = getWidth() - paddingLeft - paddingRight;
        int contentHeight = getHeight() - paddingTop - paddingBottom;

        // Draw the text.
        /*canvas.drawText(mExampleString,
                paddingLeft + (contentWidth - mTextWidth) / 2,
                paddingTop + (contentHeight + mTextHeight) / 2,
                mTextPaint);*/

        // Draw the example drawable on top of the text.
        /*if (mExampleDrawable != null) {
            mExampleDrawable.setBounds(paddingLeft, paddingTop,
                    paddingLeft + contentWidth, paddingTop + contentHeight);
            mExampleDrawable.draw(canvas);
        }*/
    }

    /**
     * Gets the example string attribute value.
     * @return The example string attribute value.
     */
    public String getExampleString() {
        return mExampleString;
    }

    /**
     * Sets the view's example string attribute value. In the example view, this string
     * is the text to draw.
     * @param exampleString The example string attribute value to use.
     */
    public void setExampleString(String exampleString) {
        mExampleString = exampleString;
        invalidateTextPaintAndMeasurements();
    }

    /**
     * Gets the example color attribute value.
     * @return The example color attribute value.
     */
    public int getExampleColor() {
        return mExampleColor;
    }

    /**
     * Sets the view's example color attribute value. In the example view, this color
     * is the font color.
     * @param exampleColor The example color attribute value to use.
     */
    public void setExampleColor(int exampleColor) {
        mExampleColor = exampleColor;
        invalidateTextPaintAndMeasurements();
    }

    /**
     * Gets the example dimension attribute value.
     * @return The example dimension attribute value.
     */
    public float getExampleDimension() {
        return mExampleDimension;
    }

    /**
     * Sets the view's example dimension attribute value. In the example view, this dimension
     * is the font size.
     * @param exampleDimension The example dimension attribute value to use.
     */
    public void setExampleDimension(float exampleDimension) {
        mExampleDimension = exampleDimension;
        invalidateTextPaintAndMeasurements();
    }

    /**
     * Gets the example drawable attribute value.
     * @return The example drawable attribute value.
     */
    public Drawable getExampleDrawable() {
        return mExampleDrawable;
    }

    /**
     * Sets the view's example drawable attribute value. In the example view, this drawable is
     * drawn above the text.
     * @param exampleDrawable The example drawable attribute value to use.
     */
    public void setExampleDrawable(Drawable exampleDrawable) {
        mExampleDrawable = exampleDrawable;
    }

    private class ArcAdapter implements ListAdapter, SpinnerAdapter {
        private SpinnerAdapter mAdapter;
        private ListAdapter mListAdapter;

        /**
         * <p>Creates a new ListAdapter wrapper for the specified adapter.</p>
         *
         * @param adapter the Adapter to transform into a ListAdapter
         */
        public ArcAdapter(SpinnerAdapter adapter) {
            this.mAdapter = adapter;
            if (adapter instanceof ListAdapter) {
                this.mListAdapter = (ListAdapter) adapter;
            }
        }

        public int getCount() {
            return mAdapter == null ? 0 : mAdapter.getCount();
        }

        public Object getItem(int position) {
            return mAdapter == null ? null : mAdapter.getItem(position);
        }

        public long getItemId(int position) {
            return mAdapter == null ? -1 : mAdapter.getItemId(position);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            return getDropDownView(position, convertView, parent);
        }

        public View getDropDownView(int position, View convertView, ViewGroup parent) {

            View view;

            if (mAdapter == null) {
                return null;
            }

            if (convertView != null) {
                view = convertView;
                // TODO: ViewHolder pattern
            } else {
                view = new FrameLayout(getContext());
            }



            return view;
        }

        public boolean hasStableIds() {
            return mAdapter != null && mAdapter.hasStableIds();
        }

        public void registerDataSetObserver(DataSetObserver observer) {
            if (mAdapter != null) {
                mAdapter.registerDataSetObserver(observer);
            }
        }

        public void unregisterDataSetObserver(DataSetObserver observer) {
            if (mAdapter != null) {
                mAdapter.unregisterDataSetObserver(observer);
            }
        }

        /**
         * If the wrapped SpinnerAdapter is also a ListAdapter, delegate this call.
         * Otherwise, return true.
         */
        public boolean areAllItemsEnabled() {
            final ListAdapter adapter = mListAdapter;
            if (adapter != null) {
                return adapter.areAllItemsEnabled();
            } else {
                return true;
            }
        }

        /**
         * If the wrapped SpinnerAdapter is also a ListAdapter, delegate this call.
         * Otherwise, return true.
         */
        public boolean isEnabled(int position) {
            final ListAdapter adapter = mListAdapter;
            if (adapter != null) {
                return adapter.isEnabled(position);
            } else {
                return true;
            }
        }

        public int getItemViewType(int position) {
            return 0;
        }

        public int getViewTypeCount() {
            return 1;
        }

        public boolean isEmpty() {
            return getCount() == 0;
        }


    }

}
