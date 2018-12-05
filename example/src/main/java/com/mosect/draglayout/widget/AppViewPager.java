package com.mosect.draglayout.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.mosect.draglayout.lib.ParentInterceptTouchHelper;

public class AppViewPager extends ViewPager {

    private ParentInterceptTouchHelper interceptTouchHelper;

    public AppViewPager(@NonNull Context context) {
        super(context);
        init();
    }

    public AppViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        interceptTouchHelper = new ParentInterceptTouchHelper(this);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        // 解决嵌套DragLayout滑动冲突
        boolean ext = interceptTouchHelper.onInterceptTouchEvent(e); // 获得是否可以滑动
        boolean result = super.onInterceptTouchEvent(e); // 父类判断的结果
        return ext && result; // 两个结果都要成立才能拦截滑动事件，不传给子View
    }
}
