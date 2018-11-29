package com.mosect.draglayout.lib;

import android.content.Context;
import android.util.TypedValue;
import android.view.MotionEvent;

class TouchTool {

    private static final float DEFAULT_CLICK_SIZE_DP = 5;

    public static final int TOUCH_TYPE_UNCERTAIN = 0;
    public static final int TOUCH_TYPE_CLICK = 1;
    public static final int TOUCH_TYPE_VERTICAL = 2;
    public static final int TOUCH_TYPE_HORIZONTAL = 3;

    private static float defaultClickSize(Context context) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_CLICK_SIZE_DP,
                context.getResources().getDisplayMetrics());
    }

    private float clickSize;
    private int touchType;
    private int verticalOrientation;
    private int horizontalOrientation;
    private float firstTouchX;
    private float firstTouchY;
    private float lastTouchX;
    private float lastTouchY;
    private float touchOffsetX;
    private float touchOffsetY;
    private float rangeX;
    private float rangeY;
    private boolean touching;

    public TouchTool(Context context) {
        this(defaultClickSize(context));
    }

    public TouchTool(float clickSize) {
        this.clickSize = clickSize;
    }

    public void onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touching = true;
                verticalOrientation = horizontalOrientation = 0;
                touchType = TOUCH_TYPE_UNCERTAIN;
                touchOffsetX = touchOffsetY = rangeX = rangeY = 0;
                firstTouchX = lastTouchX = event.getX();
                firstTouchY = lastTouchY = event.getY();
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                touching = false;
//                touchOffsetX = event.getX() - lastTouchX;
//                touchOffsetY = event.getY() - lastTouchY;
                rangeX = event.getX() - firstTouchX;
                rangeY = event.getY() - firstTouchY;
                break;

            default:
                touchOffsetX = event.getX() - lastTouchX;
                touchOffsetY = event.getY() - lastTouchY;
                rangeX = event.getX() - firstTouchX;
                rangeY = event.getY() - firstTouchY;
                switch (touchType) {
                    case TOUCH_TYPE_CLICK:
                    case TOUCH_TYPE_UNCERTAIN: {// 未确定

                        // 判断滑动方向
                        if (rangeX > 0) {
                            horizontalOrientation = 1;
                        } else if (rangeX < 0) {
                            horizontalOrientation = -1;
                        } else {
                            horizontalOrientation = 0;
                        }
                        if (rangeY > 0) {
                            verticalOrientation = 1;
                        } else if (rangeY < 0) {
                            verticalOrientation = -1;
                        } else {
                            verticalOrientation = 0;
                        }

                        // 判断滑动类型
                        float xo = Math.abs(rangeX);
                        float yo = Math.abs(rangeY);
                        if (xo > clickSize || yo > clickSize) { // 超出点击范围
                            if (xo > yo) { // 水平方向滑动
                                touchType = TOUCH_TYPE_HORIZONTAL;
                            } else { // 垂直方向滑动
                                touchType = TOUCH_TYPE_VERTICAL;
                            }
                        } else { // 点击
                            touchType = TOUCH_TYPE_CLICK;
                        }
                        break;
                    }
                    case TOUCH_TYPE_HORIZONTAL:
                    case TOUCH_TYPE_VERTICAL:
                        break;
                }
                lastTouchX = event.getX();
                lastTouchY = event.getY();
                break;
        }
    }

    public float getClickSize() {
        return clickSize;
    }

    public int getTouchType() {
        return touchType;
    }

    public int getVerticalOrientation() {
        return verticalOrientation;
    }

    public int getHorizontalOrientation() {
        return horizontalOrientation;
    }

    public float getFirstTouchX() {
        return firstTouchX;
    }

    public float getFirstTouchY() {
        return firstTouchY;
    }

    public float getLastTouchX() {
        return lastTouchX;
    }

    public float getLastTouchY() {
        return lastTouchY;
    }

    public float getTouchOffsetX() {
        return touchOffsetX;
    }

    public float getTouchOffsetY() {
        return touchOffsetY;
    }

    public float getRangeX() {
        return rangeX;
    }

    public float getRangeY() {
        return rangeY;
    }

    public void resetFirst(MotionEvent event) {
        firstTouchX = lastTouchX = event.getX();
        firstTouchY = lastTouchY = event.getY();
        rangeX = rangeY = 0;
    }

    public boolean isTouching() {
        return touching;
    }
}
