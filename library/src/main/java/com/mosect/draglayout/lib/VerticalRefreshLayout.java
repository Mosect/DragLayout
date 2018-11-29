package com.mosect.draglayout.lib;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;

/**
 * 垂直刷新布局（用于分页加载）
 */
public class VerticalRefreshLayout extends DragRefreshLayout {

    private LoadCallback loadCallback;
    private OnLoadStateChangedListener onLoadStateChangedListener;

    public VerticalRefreshLayout(Context context) {
        super(context);
        init();
    }

    public VerticalRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VerticalRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public VerticalRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setOverScroll(OVER_SCROLL_TOP | OVER_SCROLL_BOTTOM);
        setEdgeState(EDGE_STATE_DISABLE, EDGE_STATE_NORMAL, EDGE_STATE_DISABLE, EDGE_STATE_DISABLE);
    }

//    @Override
//    public void setEdgeState(int leftState, int topState, int rightState, int bottomState) {
//        // 限制部分状态更改
//        Rect oldEdgeState = getEdgeState();
//        leftState = safeState(leftState, getLayerScrollX() == 0, oldEdgeState.left);
//        topState = safeState(topState, getLayerScrollY() == 0, oldEdgeState.top);
//        rightState = safeState(rightState, getLayerScrollX() == 0, oldEdgeState.right);
//        bottomState = safeState(bottomState, getLayerScrollY() == 0, oldEdgeState.bottom);
//        super.setEdgeState(leftState, topState, rightState, bottomState);
//    }

    @Override
    protected void onEdgeStateChanged(Rect oldState, Rect state) {
        super.onEdgeStateChanged(oldState, state);
        if (oldState.top != state.top) {
            if (null != onLoadStateChangedListener) {
                onLoadStateChangedListener
                        .onRefreshStateChanged(this, oldState.top, state.top);
            }
            if (state.top == EDGE_STATE_LOADING) {
                onRefresh();
                if (null != loadCallback) {
                    loadCallback.onRefresh(this);
                }
            }
        }

        if (oldState.bottom != state.bottom) {
            if (null != onLoadStateChangedListener) {
                onLoadStateChangedListener
                        .onLoadMoreStateChanged(this, oldState.bottom, state.bottom);
            }
            if (state.bottom == EDGE_STATE_LOADING) {
                onLoadMore();
                if (null != loadCallback) {
                    loadCallback.onLoadMore(this);
                }
            }
        }
    }

    public LoadCallback getLoadCallback() {
        return loadCallback;
    }

    public void setLoadCallback(LoadCallback loadCallback) {
        this.loadCallback = loadCallback;
    }

    public void postRefresh(final boolean openEdge) {
        postAfterLayout(new Runnable() {
            @Override
            public void run() {
                doRefresh(openEdge);
            }
        });
    }

    public void doRefresh(boolean openEdge) {
        Rect state = getEdgeState();
        if (state.top != EDGE_STATE_LOADING) {
            state.top = EDGE_STATE_LOADING;
            state.bottom = EDGE_STATE_DISABLE;
            setEdgeState(state);
            if (openEdge) {
                openTop(true);
            }
        }
    }

    public void postLoadMore(final boolean openEdge) {
        postAfterLayout(new Runnable() {
            @Override
            public void run() {
                doLoadMore(openEdge);
            }
        });
    }

    public void doLoadMore(boolean openEdge) {
        Rect state = getEdgeState();
        if (state.bottom != EDGE_STATE_LOADING) {
            state.bottom = EDGE_STATE_LOADING;
            setEdgeState(state);
            if (openEdge) {
                openBottom(true);
            }
        }
    }

    protected void onRefresh() {
    }

    protected void onLoadMore() {
    }

    /**
     * 完成读取
     *
     * @param hasMore 是否有更多数据
     */
    public void finishLoad(boolean hasMore) {
        Rect state = getEdgeState();
        if (state.top == EDGE_STATE_LOADING) {
            if (getLayerScrollY() == 0) {
                // 处于关闭状态
                state.top = EDGE_STATE_NORMAL;
            } else {
                state.top = EDGE_STATE_FINISH;
            }
        }

        if (hasMore) {
            if (getLayerScrollY() == 0) {
                // 处于关闭状态
                state.bottom = EDGE_STATE_NORMAL;
            } else {
                state.bottom = EDGE_STATE_FINISH;
            }
        } else {
            state.bottom = EDGE_STATE_NONE;
        }
        setEdgeState(state);
    }

    public int getRefreshState() {
        return getEdgeState().top;
    }

    public int getLoadMoreState() {
        return getEdgeState().bottom;
    }

    public boolean isRefreshing() {
        return getRefreshState() == EDGE_STATE_LOADING;
    }

    public boolean isLoadingMore() {
        return getLoadMoreState() == EDGE_STATE_LOADING;
    }

    public OnLoadStateChangedListener getOnLoadStateChangedListener() {
        return onLoadStateChangedListener;
    }

    public void setOnLoadStateChangedListener(OnLoadStateChangedListener onLoadStateChangedListener) {
        this.onLoadStateChangedListener = onLoadStateChangedListener;
    }

//    private int safeState(int state, boolean closed, int oldState) {
//        switch (state) {
//            case EDGE_STATE_DISABLE:
//            case EDGE_STATE_LOADING:
//            case EDGE_STATE_NONE:
//            case EDGE_STATE_NORMAL:
//                return state;
//
//            case EDGE_STATE_FINISH:
//                if (closed) { // 如果关闭了周边，变成正常状态
//                    return EDGE_STATE_NORMAL;
//                }
//                return state;
//
//            case EDGE_STATE_READY: // 无法设置成预备状态
//            default:
//                return oldState;
//        }
//    }

    public interface LoadCallback {

        void onRefresh(VerticalRefreshLayout view);

        void onLoadMore(VerticalRefreshLayout view);
    }

    public interface OnLoadStateChangedListener {

        void onRefreshStateChanged(VerticalRefreshLayout view, int oldState, int state);

        void onLoadMoreStateChanged(VerticalRefreshLayout view, int oldState, int state);
    }
}
