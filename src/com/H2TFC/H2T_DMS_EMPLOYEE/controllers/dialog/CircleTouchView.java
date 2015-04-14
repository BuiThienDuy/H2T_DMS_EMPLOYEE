package com.H2TFC.H2T_DMS_EMPLOYEE.controllers.dialog;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by c4sau on 30/03/2015.
 */
public class CircleTouchView extends View {
    private static final int MODE_PINCH = 0;
    private static final int MODE_DONT_CARE = 1;

    ShapeDrawable mCircleDrawable;
    int mTouchMode = MODE_DONT_CARE;

    public CircleTouchView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mCircleDrawable = new ShapeDrawable(new OvalShape());
        mCircleDrawable.getPaint().setColor(0x66FFFFFF);
    }

    public CircleTouchView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleTouchView(Context context) {
        this(context, null, 0);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mCircleDrawable.setBounds(0, 0, 0, 0);
                invalidate();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                prepareCircleDrawing(event);
                break;
            case MotionEvent.ACTION_MOVE:
                if (mTouchMode == MODE_PINCH) {
                    prepareCircleDrawing(event);
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if (event.getActionIndex() <= 1) {
                    mTouchMode = MODE_DONT_CARE;
                }
                break;
            default:
                super.onTouchEvent(event);
        }

        return true;
    }

    private void prepareCircleDrawing(MotionEvent event) {
        int top, right, bottom, left;
        int index = event.getActionIndex();

        if (index > 1) {
            return;
        }
        mTouchMode = MODE_PINCH;
        if (event.getX(0) < event.getX(1)) {
            left = (int) event.getX(0);
            right = (int) event.getX(1);
        } else {
            left = (int) event.getX(1);
            right = (int) event.getX(0);
        }

        if (event.getY(0) < event.getY(1)) {
            top = (int) event.getY(0);
            bottom = (int) event.getY(1);
        } else {
            top = (int) event.getY(1);
            bottom = (int) event.getY(0);
        }

        mCircleDrawable.setBounds(left, top, right, bottom);

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mCircleDrawable.draw(canvas);
    }
}