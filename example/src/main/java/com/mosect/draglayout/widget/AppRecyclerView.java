package com.mosect.draglayout.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.mosect.draglayout.lib.ParentInterceptTouchHelper;

public class AppRecyclerView extends RecyclerView {

    private ParentInterceptTouchHelper interceptTouchHelper;

    public AppRecyclerView(@NonNull Context context) {
        super(context);
        init();
    }

    public AppRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AppRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        interceptTouchHelper = new ParentInterceptTouchHelper(this);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        // 解决DragLayout滑动冲突
        boolean ext = interceptTouchHelper.onInterceptTouchEvent(e); // 获得是否可以滑动
        boolean result = super.onInterceptTouchEvent(e); // 父类判断的结果
        return ext && result; // 两个结果都要成立才能拦截滑动事件，不传给子View
    }
}
