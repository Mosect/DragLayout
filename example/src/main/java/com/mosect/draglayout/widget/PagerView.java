package com.mosect.draglayout.widget;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.mosect.draglayout.example.R;
import com.mosect.draglayout.lib.VerticalRefreshLayout;

/**
 * 分页加载控件，在项目中不应该直接使用VerticalRefreshLayout，根据app ui风格先封装出一个通用的分页控件
 */
public class PagerView extends VerticalRefreshLayout {

    private RecyclerView content;
    private TextView header;
    private TextView footer;
    private OnLoadStateChangedListener onLoadStateChangedListener;

    public PagerView(Context context) {
        super(context);
        init();
    }

    public PagerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PagerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.widget_pagerview, this, true);
        content = findViewById(R.id.rv_content);
        header = findViewById(R.id.tv_header);
        footer = findViewById(R.id.tv_footer);
        super.setOnLoadStateChangedListener(new OnLoadStateChangedListener() {
            @Override
            public void onRefreshStateChanged(VerticalRefreshLayout view, int oldState, int state) {
                updateHeaderState();
                if (null != onLoadStateChangedListener) {
                    onLoadStateChangedListener.onRefreshStateChanged(view, oldState, state);
                }
            }

            @Override
            public void onLoadMoreStateChanged(VerticalRefreshLayout view, int oldState, int state) {
                updateFooterState();
                if (null != onLoadStateChangedListener) {
                    onLoadStateChangedListener.onLoadMoreStateChanged(view, oldState, state);
                }
            }
        });
    }

    @Override
    public void setOnLoadStateChangedListener(OnLoadStateChangedListener onLoadStateChangedListener) {
        this.onLoadStateChangedListener = onLoadStateChangedListener;
    }

    @Override
    public OnLoadStateChangedListener getOnLoadStateChangedListener() {
        return onLoadStateChangedListener;
    }

    public RecyclerView getContent() {
        return content;
    }

    public TextView getHeader() {
        return header;
    }

    public TextView getFooter() {
        return footer;
    }

    public void updateHeaderState() {
        int state = getEdgeState().top;
        switch (state) {
            case EDGE_STATE_NONE:
                header.setText(R.string.top_none);
                break;
            case EDGE_STATE_NORMAL:
                header.setText(R.string.top_normal);
                break;
            case EDGE_STATE_FINISH:
                // 成功或失败
                // success or failed
                header.setText(R.string.top_finish);
                break;
            case EDGE_STATE_READY:
                header.setText(R.string.top_ready);
                break;
            case EDGE_STATE_LOADING:
                header.setText(R.string.top_loading);
                break;
        }
    }

    public void updateFooterState() {
        int state = getEdgeState().bottom;
        switch (state) {
            case EDGE_STATE_NONE:
                footer.setText(R.string.bottom_none);
                break;
            case EDGE_STATE_NORMAL:
                footer.setText(R.string.bottom_normal);
                break;
            case EDGE_STATE_FINISH:
                // 成功或失败
                // success or failed
                footer.setText(R.string.bottom_finish);
                break;
            case EDGE_STATE_READY:
                footer.setText(R.string.bottom_ready);
                break;
            case EDGE_STATE_LOADING:
                footer.setText(R.string.bottom_loading);
                break;
        }
    }
}
