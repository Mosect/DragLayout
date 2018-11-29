package com.mosect.draglayout.lib;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;

/**
 * 折叠布局，主要是实现周边打开的规则
 */
public class FlowLayout extends DragLayout {

    private int openVy; // 打开上、下面的速度
    private int openVx; // 打开左、右面的速度

    public FlowLayout(Context context) {
        super(context);
        init();
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        openVx = openVy = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                DragLayout.DEFAULT_SCROLL_VELOCITY_DP,
                getContext().getResources().getDisplayMetrics());
    }

    @Override
    protected boolean canOpenTop(float xv, float yv) {
        if (yv > 0) {
            // 下拉
            int min = getVerticalLayerScrollMin();
            int sy = getLayerScrollY();
            return Math.abs(yv) >= openVy || sy <= min / 2;
        }
        return super.canOpenTop(xv, yv);
    }

    @Override
    protected boolean canOpenBottom(float xv, float yv) {
        if (yv < 0) {
            // 上拉
            int max = getVerticalLayerScrollMax();
            int sy = getLayerScrollY();
            return Math.abs(yv) >= openVy || sy >= max / 2;
        }
        return super.canOpenBottom(xv, yv);
    }

    @Override
    protected boolean canOpenLeft(float xv, float yv) {
        if (xv > 0) {
            // 下拉
            int min = getHorizontalLayerScrollMin();
            int sx = getLayerScrollX();
            return Math.abs(xv) >= openVx || sx <= min / 2;
        }
        return super.canOpenLeft(xv, yv);
    }

    @Override
    protected boolean canOpenRight(float xv, float yv) {
        if (xv < 0) {
            // 上拉
            int max = getHorizontalLayerScrollMax();
            int sx = getLayerScrollX();
            return Math.abs(xv) >= openVx || sx >= max / 2;
        }
        return super.canOpenRight(xv, yv);
    }
}
