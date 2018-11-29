package com.mosect.draglayout.lib;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * 拖拽刷新布局，可用于分页
 */
public class DragRefreshLayout extends DragLayout {

    /**
     * 周边状态：关闭，不可打开
     */
    public static final int EDGE_STATE_DISABLE = -1;
    /**
     * 周边状态：无，表示无法操作，此状态不能通过滑动更改且
     */
    public static final int EDGE_STATE_NONE = 0;
    /**
     * 周边状态：正常，可以通过滑动更改
     */
    public static final int EDGE_STATE_NORMAL = 1;
    /**
     * 周边状态：准备中，特殊状态，在触摸滑动超出滑动范围后由EDGE_STATE_NORMAL变成的状态，
     * 无法手动设置成此状态
     */
    public static final int EDGE_STATE_READY = 2;
    /**
     * 周边状态：读取中，可以由由EDGE_STATE_NORMAL或EDGE_STATE_READY通过滑动变成此状态
     * 无法通过滑动取消此状态，必须通过{@link DragRefreshLayout#setEdgeState(Rect) setEdgeState(Rect)}
     * 或{@link DragRefreshLayout#setEdgeState(int, int, int, int) setEdgeState(int, int, int, int)}
     * 设置状态才能更改。
     */
    public static final int EDGE_STATE_LOADING = 3;
    /**
     * 完成状态：读取完成，手动设置状态，一般都是由EDGE_STATE_LOADING变成
     */
    public static final int EDGE_STATE_FINISH = 4;

    private Rect edgeState = new Rect(EDGE_STATE_NONE, EDGE_STATE_NONE,
            EDGE_STATE_NONE, EDGE_STATE_NONE);
    private Rect oldEdgeState = new Rect();
    private Rect newEdgeState = new Rect();
    private boolean touching = false;
    private OnEdgeStateChangedListener onEdgeStateChangedListener;
    // 正在滑动的周边，0表示未确定，1表示左边，2表示上边，3表示右边，4表示下边
    private int scrollEdge = 0;

    public DragRefreshLayout(Context context) {
        super(context);
        init(null);
    }

    public DragRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public DragRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public DragRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            touching = true;
        }
        boolean result = super.onInterceptTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_CANCEL ||
                event.getAction() == MotionEvent.ACTION_UP) {
            touching = false;
        }
        return result;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            performClick();
            // 记录旧状态
            touching = true;
        }
        if (event.getAction() == MotionEvent.ACTION_CANCEL ||
                event.getAction() == MotionEvent.ACTION_UP) {
            touching = false;
        }
        boolean result = super.onTouchEvent(event);
        return result;
    }

    @Override
    protected void onLayerScrollChanged(int oldX, int oldY, int newX, int newY) {
        super.onLayerScrollChanged(oldX, oldY, newX, newY);

        // 确定正在滑动的周边
        if (touching) { // 触摸滑动中
            if (scrollEdge == 0) { // 未确定滑动的边
                if (newX < 0) {
                    scrollEdge = 1; // 左边
                } else if (newX > 0) {
                    scrollEdge = 3; // 右边
                } else if (newY < 0) {
                    scrollEdge = 2; // 上边
                } else if (newY > 0) {
                    scrollEdge = 4; // 下边
                }
            }
        } else { // 没有触摸滑动
            if (newX < 0) {
                scrollEdge = 1;
            } else if (newX > 0) {
                scrollEdge = 3;
            } else if (newY < 0) {
                scrollEdge = 2;
            } else if (newY > 0) {
                scrollEdge = 4;
            } else {
                scrollEdge = 0;
            }
        }

        oldEdgeState.set(edgeState); // 保存旧状态

        // 打开左边
        if (newX <= getHorizontalLayerScrollMin()) {
            if (touching) { // 触摸滑动中
                if (edgeState.left == EDGE_STATE_NORMAL) { // 正常状态变成准备模式
                    edgeState.left = EDGE_STATE_READY;
                }
            } else { // 没有触摸滑动中，正常状态、读取状态需要变成读取状态
                if (edgeState.left == EDGE_STATE_NORMAL ||
                        edgeState.left == EDGE_STATE_READY) {
                    edgeState.left = EDGE_STATE_LOADING;
                }
            }
        }

        // 打开右边
        if (newX >= getHorizontalLayerScrollMax()) {
            if (touching) { // 触摸滑动中
                if (edgeState.right == EDGE_STATE_NORMAL) { // 正常状态变成准备模式
                    edgeState.right = EDGE_STATE_READY;
                }
            } else { // 没有触摸滑动中，正常状态、读取状态需要变成读取状态
                if (edgeState.right == EDGE_STATE_NORMAL ||
                        edgeState.right == EDGE_STATE_READY) {
                    edgeState.right = EDGE_STATE_LOADING;
                }
            }
        }

        // 左右两边都被关闭
        if (newX == 0) {
            if (edgeState.left == EDGE_STATE_FINISH) {
                edgeState.left = EDGE_STATE_NORMAL;
            }
            if (edgeState.right == EDGE_STATE_FINISH) {
                edgeState.right = EDGE_STATE_NORMAL;
            }
        }


        // 打开上边
        if (newY <= getVerticalLayerScrollMin()) {
            if (touching) { // 触摸滑动中
                if (edgeState.top == EDGE_STATE_NORMAL) { // 正常状态变成准备模式
                    edgeState.top = EDGE_STATE_READY;
                }
            } else { // 没有触摸滑动中，正常状态、读取状态需要变成读取状态
                if (edgeState.top == EDGE_STATE_NORMAL ||
                        edgeState.top == EDGE_STATE_READY) {
                    edgeState.top = EDGE_STATE_LOADING;
                }
            }
        }

        // 打开下边
        if (newY >= getVerticalLayerScrollMax()) {
            if (touching) { // 触摸滑动中
                if (edgeState.bottom == EDGE_STATE_NORMAL) { // 正常状态变成准备模式
                    edgeState.bottom = EDGE_STATE_READY;
                }
            } else { // 没有触摸滑动中，正常状态、读取状态需要变成读取状态
                if (edgeState.bottom == EDGE_STATE_NORMAL ||
                        edgeState.bottom == EDGE_STATE_READY) {
                    edgeState.bottom = EDGE_STATE_LOADING;
                }
            }
        }

        // 上下两边都被关闭
        if (newY == 0) {
            if (edgeState.top == EDGE_STATE_FINISH) {
                edgeState.top = EDGE_STATE_NORMAL;
            }
            if (edgeState.bottom == EDGE_STATE_FINISH) {
                edgeState.bottom = EDGE_STATE_NORMAL;
            }
        }

        if (!oldEdgeState.equals(edgeState)) {
            // 发生更改
            changeEdgeState();
        }
    }

    @Override
    protected boolean canHorizontalScrollTo(int x) {
        if (isNoEdgeScroll()|| (x <= 0 && scrollEdge == 1) || (x >= 0 && scrollEdge == 3)) {
            // 未确定滑动的周边或者超出已确定周边的滑动范围
            return super.canHorizontalScrollTo(x);
        }
        return false;
    }

    @Override
    protected boolean canVerticalScrollTo(int y) {
        if (isNoEdgeScroll() || (y <= 0 && scrollEdge == 2) || (y >= 0 && scrollEdge == 4)) {
            // 未确定滑动的周边或者超出已确定周边的滑动范围
            return super.canVerticalScrollTo(y);
        }
        return false;
    }

    @Override
    public int getHorizontalLayerScrollMin() {
        if (isRightEdgeScroll() || edgeState.left == EDGE_STATE_DISABLE) {
            // 在右边滑动或者左边被禁止打开
            return 0;
        }
        return super.getHorizontalLayerScrollMin();
    }

    @Override
    public int getHorizontalLayerScrollMax() {
        if (isLeftEdgeScroll() || edgeState.right == EDGE_STATE_DISABLE) {
            // 在左边滑动或者右边被禁止打开
            return 0;
        }
        return super.getHorizontalLayerScrollMax();
    }

    @Override
    public int getVerticalLayerScrollMin() {
        if (isBottomEdgeScroll() || edgeState.top == EDGE_STATE_DISABLE) {
            // 在下边滑动或者上边被禁止打开
            return 0;
        }
        return super.getVerticalLayerScrollMin();
    }

    @Override
    public int getVerticalLayerScrollMax() {
        if (isTopEdgeScroll() || edgeState.bottom == EDGE_STATE_DISABLE) {
            // 在上边滑动或者下边被禁止打开
            return 0;
        }
        return super.getVerticalLayerScrollMax();
    }

    @Override
    protected boolean canOpenTop(float xv, float yv) {
        if (edgeState.top == EDGE_STATE_DISABLE) {
            // 上边被禁止打开
            return false;
        }
        return super.canOpenTop(xv, yv);
    }

    @Override
    protected boolean canOpenLeft(float xv, float yv) {
        if (edgeState.left == EDGE_STATE_DISABLE) {
            // 左边被禁止打开
            return false;
        }
        return super.canOpenLeft(xv, yv);
    }

    @Override
    protected boolean canOpenBottom(float xv, float yv) {
        if (edgeState.bottom == EDGE_STATE_DISABLE) {
            // 下边被禁止打开
            return false;
        }
        return super.canOpenBottom(xv, yv);
    }

    @Override
    protected boolean canOpenRight(float xv, float yv) {
        if (edgeState.right == EDGE_STATE_DISABLE) {
            // 右边被禁止打开
            return false;
        }
        return super.canOpenRight(xv, yv);
    }

    protected void onEdgeStateChanged(Rect oldState, Rect state) {

    }

    /**
     * 设置边缘状态
     *
     * @param leftState   左状态
     * @param topState    上状态
     * @param rightState  右状态
     * @param bottomState 下状态
     */
    public void setEdgeState(int leftState, int topState, int rightState, int bottomState) {
        oldEdgeState.set(edgeState); // 保存旧状态
        this.edgeState.set(leftState, topState, rightState, bottomState);
        if (!oldEdgeState.equals(edgeState)) { // 状态发生更改
            changeEdgeState();
        }
    }

    /**
     * 设置边缘状态
     *
     * @param edgeState 边缘状态
     */
    public void setEdgeState(Rect edgeState) {
        if (null != edgeState) {
            setEdgeState(edgeState.left, edgeState.top, edgeState.right, edgeState.bottom);
        }
    }

    /**
     * 获取边缘状态
     *
     * @return 边缘状态
     */
    public Rect getEdgeState() {
        newEdgeState.set(edgeState);
        return newEdgeState;
    }

    public boolean isLeftEdgeScroll() {
        return scrollEdge == 1;
    }

    public boolean isTopEdgeScroll() {
        return scrollEdge == 2;
    }

    public boolean isRightEdgeScroll() {
        return scrollEdge == 3;
    }

    public boolean isBottomEdgeScroll() {
        return scrollEdge == 4;
    }

    public boolean isNoEdgeScroll() {
        return scrollEdge == 0;
    }

    public OnEdgeStateChangedListener getOnEdgeStateChangedListener() {
        return onEdgeStateChangedListener;
    }

    public void setOnEdgeStateChangedListener(OnEdgeStateChangedListener onEdgeStateChangedListener) {
        this.onEdgeStateChangedListener = onEdgeStateChangedListener;
    }

    private void changeEdgeState() {
        newEdgeState.set(edgeState);
        onEdgeStateChanged(oldEdgeState, newEdgeState);
        if (null != onEdgeStateChangedListener) {
            onEdgeStateChangedListener.onEdgeStateChanged(this, oldEdgeState, newEdgeState);
        }
    }

    /**
     * 边缘状态更改监听器
     */
    public interface OnEdgeStateChangedListener {

        void onEdgeStateChanged(DragRefreshLayout view, Rect oldState, Rect state);
    }
}
