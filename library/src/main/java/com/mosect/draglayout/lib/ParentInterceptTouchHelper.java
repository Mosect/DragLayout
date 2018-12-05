package com.mosect.draglayout.lib;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * 如果将DragLayout添加在可滑动的ViewGroup中，需要在
 * {@link ViewGroup#onInterceptTouchEvent(MotionEvent) ViewGroup.onInterceptTouchEvent}
 * 方法使用此工具类进行拦截判断，如果此类的
 * {@link ParentInterceptTouchHelper#onInterceptTouchEvent(MotionEvent) ParentInterceptTouchHelper.onInterceptTouchEvent}
 * 返回false，父视图也应该返回false
 */
public class ParentInterceptTouchHelper {

    private ViewGroup parent;
    private TouchTool touchTool;

    public ParentInterceptTouchHelper(ViewGroup parent) {
        this.parent = parent;
        touchTool = new TouchTool(parent.getContext());
    }

    public boolean onInterceptTouchEvent(MotionEvent event) {
        touchTool.onTouchEvent(event);
        switch (touchTool.getTouchType()) {
            case TouchTool.TOUCH_TYPE_HORIZONTAL: // 水平滑动
                return !canChildrenScrollHorizontally(event, -touchTool.getHorizontalOrientation())
                        && canParentScrollHorizontally(parent, -touchTool.getHorizontalOrientation());

            case TouchTool.TOUCH_TYPE_VERTICAL: // 垂直滑动
                return !canChildrenScrollVertically(event, -touchTool.getVerticalOrientation())
                        && canParentScrollVertically(parent, -touchTool.getVerticalOrientation());

            case TouchTool.TOUCH_TYPE_UNCERTAIN: // 未确定
            case TouchTool.TOUCH_TYPE_CLICK: // 点击事件
            default: // 其他未知操作
                return false;
        }
    }

    /**
     * 判断子视图是否可以垂直滑动
     *
     * @param event     滑动事件
     * @param direction 方向：负数表示ScrollY值变小的方向；整数表示ScrollY值变大的方向
     * @return true，子View可以滑动
     */
    protected boolean canChildrenScrollVertically(MotionEvent event, int direction) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            int index = parent.getChildCount() - 1 - i;
            View child = parent.getChildAt(index);
            if (child.getVisibility() == View.VISIBLE && child.isEnabled()) {
                float x = event.getX();
                float y = event.getY();
                if (x >= child.getLeft() && x <= child.getRight() &&
                        y >= child.getTop() && y <= child.getBottom()) {
                    if (canChildScrollVertically(child, direction)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 判断子View是否可以垂直滑动
     *
     * @param child     子View
     * @param direction 方向：负数表示ScrollY值变小的方向；整数表示ScrollY值变大的方向
     * @return true，可以滑动
     */
    protected boolean canChildScrollVertically(View child, int direction) {
        return child.canScrollVertically(direction);
    }

    /**
     * 判断子视图是否可以水平滑动
     *
     * @param event     滑动事件
     * @param direction 方向：负数表示ScrollX值变小的方向；整数表示ScrollX值变大的方向
     * @return true，子View可以滑动
     */
    protected boolean canChildrenScrollHorizontally(MotionEvent event, int direction) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            int index = parent.getChildCount() - 1 - i;
            View child = parent.getChildAt(index);
            if (child.getVisibility() == View.VISIBLE && child.isEnabled()) {
                float x = event.getX();
                float y = event.getY();
                if (x >= child.getLeft() && x <= child.getRight() &&
                        y >= child.getTop() && y <= child.getBottom()) {
                    if (canChildScrollHorizontally(child, direction)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 判断子View是否可以水平滑动
     *
     * @param child     子View
     * @param direction 方向：负数表示ScrollX值变小的方向；整数表示ScrollX值变大的方向
     * @return true，可以滑动
     */
    protected boolean canChildScrollHorizontally(View child, int direction) {
        return child.canScrollHorizontally(direction);
    }

    /**
     * 判断父视图是否可以水平滑动
     *
     * @param parent    父视图
     * @param direction 方向
     * @return true，可以水平滑动
     */
    protected boolean canParentScrollHorizontally(ViewGroup parent, int direction) {
        return parent.canScrollHorizontally(direction);
    }

    /**
     * 判断父视图是否可以垂直滑动
     *
     * @param parent    父视图
     * @param direction 方向
     * @return true，可以水平滑动
     */
    protected boolean canParentScrollVertically(ViewGroup parent, int direction) {
        return parent.canScrollVertically(direction);
    }
}
