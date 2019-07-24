package com.mosect.draglayout.lib;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.Scroller;

import com.mosect.viewutils.GestureHelper;
import com.mosect.viewutils.MeasureUtils;

import java.util.LinkedList;

public class DragLayout extends ViewGroup {

    /**
     * 默认滑动速度，dip/秒
     */
    public static final int DEFAULT_SCROLL_VELOCITY_DP = 300;
    /**
     * 不可以超出滑动范围
     */
    public static final int OVER_SCROLL_NONE = 0x00;
    /**
     * 左边可以超出滑动范围
     */
    public static final int OVER_SCROLL_LEFT = 0x01;
    /**
     * 上边可以超出滑动范围
     */
    public static final int OVER_SCROLL_TOP = 0x02;
    /**
     * 右边可以超出滑动范围
     */
    public static final int OVER_SCROLL_RIGHT = 0x04;
    /**
     * 下边可以超出滑动范围
     */
    public static final int OVER_SCROLL_BOTTOM = 0x08;
    /**
     * 所有方向都可以超出滑动范围
     */
    public static final int OVER_SCROLL_ALL =
            OVER_SCROLL_LEFT | OVER_SCROLL_TOP | OVER_SCROLL_RIGHT | OVER_SCROLL_BOTTOM;

    private Rect edgeSize = new Rect(); // 边缘大小
    private int layerScrollX; // 滑动层滑动位置X
    private int layerScrollY; // 滑动层滑动位置Y
    private Rect centerRect = new Rect(); // 中间的区间
    private int overScroll = OVER_SCROLL_ALL; // 是否可以超出滑动范围
    private int scrollVelocity; // 滑动速度
    /**
     * 最大滑动时间，大于0，表示smoothLayer操作有最大时间限制，如果按照scrollVelocity的速度没能在最大
     * 时间内完成，则smoothLayer操作不会按照scrollVelocity，其速度会被重新计算成刚好在最大时间内完成
     * 的速度，此速度默认为0，即关闭最大时间限制
     */
    private int maxScrollTime;
    private LinkedList<Runnable> afterLayoutRunnableList;
    private boolean attached = false; // 是否添加到窗口系统

    private Rect outRect = new Rect(); // 辅助Gravity计算Rect
    private Rect containerRect = new Rect(); // 辅助Gravity计算Rect
    private GestureHelper gestureHelper; // 触摸滑动辅助工具
    private VelocityTracker velocityTracker; // 速度辅助工具
    private int touchScrollStartX; // 开始触摸时滑动层的滑动位置X
    private int touchScrollStartY; // 开始触摸时滑动层的滑动位置Y
    private float touchStartX; // 开始触摸滑动位置：X
    private float touchStartY; // 开始触摸滑动位置：Y
    private boolean touching; // 是否正在滑动中
    private Scroller layerScroller; // 滑动层的滑动器
    private long viewInfoCode = 0; // 视图信息hash code，如果视图发生变化，此hashCode会发生改变

    private OnLayerScrollChangedListener onLayerScrollChangedListener;
    private CanOpenEdgeCallback canOpenEdgeCallback;
    private CanChildScrollCallback canChildScrollCallback;
    private Runnable updateLayerScrollRunnable;

    public DragLayout(Context context) {
        super(context);
        init(null);
    }

    public DragLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public DragLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DragLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        scrollVelocity = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                DEFAULT_SCROLL_VELOCITY_DP, getContext().getResources().getDisplayMetrics());
        if (null != attrs) {
            TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.DragLayout);
            overScroll = ta.getInt(R.styleable.DragLayout_overScroll, OVER_SCROLL_ALL);
            scrollVelocity = ta.getDimensionPixelSize(
                    R.styleable.DragLayout_scrollVelocity, scrollVelocity);
            ta.recycle();
        }
        gestureHelper = GestureHelper.createDefault(getContext());
        layerScroller = new Scroller(getContext());
        velocityTracker = VelocityTracker.obtain();
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (null != afterLayoutRunnableList && !afterLayoutRunnableList.isEmpty()) {
                    while (afterLayoutRunnableList.size() > 0) {
                        afterLayoutRunnableList.removeFirst().run();
                    }
                }
            }
        });
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
        return new LayoutParams(lp);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        handleTouchEvent(event);
        switch (gestureHelper.getGesture()) {
            case GestureHelper.GESTURE_LEFT: // 左滑
                if (getLayerScrollX() != 0) {
                    return true;
                }
                // 子View不可以滑动并且此View可以滑动
                return !canChildrenScrollHorizontally(event, 1) &&
                        canLayerScrollHorizontally(1);

            case GestureHelper.GESTURE_RIGHT: // 右滑
                if (getLayerScrollX() != 0) {
                    return true;
                }
                // 子View不可以滑动并且此View可以滑动
                return !canChildrenScrollHorizontally(event, -1) &&
                        canLayerScrollHorizontally(-1);

            case GestureHelper.GESTURE_UP: // 上滑
                if (getLayerScrollY() != 0) {
                    return true;
                }
                // 子View不可以滑动并且此View可以滑动
                return !canChildrenScrollVertically(event, 1) &&
                        canLayerScrollVertically(1);

            case GestureHelper.GESTURE_DOWN:
                if (getLayerScrollY() != 0) {
                    return true;
                }
                // 子View不可以滑动并且此View可以滑动
                return !canChildrenScrollVertically(event, -1) &&
                        canLayerScrollVertically(-1);

            default: // 其他操作
                if (event.getAction() == MotionEvent.ACTION_UP ||
                        event.getAction() == MotionEvent.ACTION_CANCEL) {
                    // 停止触摸
                    releaseTouch();
                }
                return false;
        }
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            performClick();
        }
        handleTouchEvent(event);

        boolean resetTouch = false;
        boolean vertical = false;
        boolean horizontal = false;
        switch (gestureHelper.getGesture()) {
            case GestureHelper.GESTURE_RIGHT:
            case GestureHelper.GESTURE_LEFT:  // 水平滑动
                if (getLayerScrollY() != 0) {
                    // 滑动层已在垂直方向滑动，不能受水平滑动而改变滑动层滑动方向
                    vertical = true;
                } else {
                    horizontal = true;
                }
                break; // case TouchTool.TOUCH_TYPE_HORIZONTAL

            case GestureHelper.GESTURE_DOWN:
            case GestureHelper.GESTURE_UP: // 垂直滑动
                if (getLayerScrollX() != 0) {
                    // 滑动层已在水平方向滑动，不能受垂直滑动而改变滑动层滑动方向
                    horizontal = true;
                } else {
                    vertical = true;
                }
                break; // case TouchTool.TOUCH_TYPE_VERTICAL

            default:
                if (event.getAction() == MotionEvent.ACTION_UP ||
                        event.getAction() == MotionEvent.ACTION_CANCEL) {
                    // 停止触摸
                    releaseTouch();
                }
                break;
        }

        if (vertical) { // 垂直方向
            float rangeY = event.getY() - touchStartY;
            int dy = (int) (touchScrollStartY - rangeY);
            if (!canVerticalScrollTo(dy)) { // 判断是否可以滑动到指定位置
                resetTouch = true;
                if (dy < 0) {
                    dy = getVerticalLayerScrollMin();
                } else if (dy > 0) {
                    dy = getVerticalLayerScrollMax();
                }
            }
            layerScrollTo(getLayerScrollX(), dy);

            // 处理停止触摸情况
            if (event.getAction() == MotionEvent.ACTION_UP ||
                    event.getAction() == MotionEvent.ACTION_CANCEL) {
                velocityTracker.computeCurrentVelocity(1000);
                // 触摸停止
                if (getLayerScrollY() < 0) {
                    // 上方
                    if (velocityTracker.getYVelocity() > 0) {
                        // 触摸向下滑动
                        if (canOpenTop(velocityTracker.getXVelocity(), velocityTracker.getYVelocity())) {
                            // 可以打开上方视图
                            smoothLayerScrollTo(getLayerScrollX(), getVerticalLayerScrollMin());
                        } else { // 不可以打开上方视图，恢复内容视图位置
                            smoothLayerScrollTo(0, 0);
                        }
                    } else {
                        // 触摸向上滑动，表示不打开上方视图，恢复内容视图位置
                        smoothLayerScrollTo(0, 0);
                    }

                } else if (getLayerScrollY() > 0) {
                    // 下方
                    if (velocityTracker.getYVelocity() < 0) {
                        // 触摸向上滑动
                        if (canOpenBottom(velocityTracker.getXVelocity(), velocityTracker.getYVelocity())) {
                            // 可以打开下方视图
//                            System.out.println("onTouchEvent:canOpenBottom");
                            smoothLayerScrollTo(getLayerScrollX(), getVerticalLayerScrollMax());
                        } else { // 不可以打开下方视图，恢复内容视图位置
//                            System.out.println("onTouchEvent:can'tOpenBottom");
                            smoothLayerScrollTo(0, 0);
                        }
                    } else {
                        // 触摸向下滑动，表示不打开下方视图，恢复内容视图位置
                        smoothLayerScrollTo(0, 0);
                    }
                }
            }

        } else if (horizontal) { // 水平方向
            float rangeX = event.getX() - touchStartX;
            int dx = (int) (touchScrollStartX - rangeX);
            if (!canHorizontalScrollTo(dx)) { // 判断是否可以滑动到指定位置
                resetTouch = true;
                if (dx < 0) {
                    dx = getHorizontalLayerScrollMin();
                } else if (dx > 0) {
                    dx = getHorizontalLayerScrollMax();
                }
            }
            layerScrollTo(dx, getScrollY());

            // 处理停止触摸情况
            if (event.getAction() == MotionEvent.ACTION_UP ||
                    event.getAction() == MotionEvent.ACTION_CANCEL) {
                velocityTracker.computeCurrentVelocity(1000);
                // 触摸停止
                if (getLayerScrollX() < 0) {
                    // 左边
                    if (velocityTracker.getXVelocity() > 0) {
                        // 触摸向右滑动
                        if (canOpenLeft(velocityTracker.getXVelocity(), velocityTracker.getYVelocity())) {
                            // 可以打开左边
//                            System.out.println("onTouchEvent:canOpenLeft");
                            smoothLayerScrollTo(getHorizontalLayerScrollMin(), getLayerScrollY());
                        } else { // 不可以打开左边，恢复内容视图位置
                            smoothLayerScrollTo(0, 0);
                        }
                    } else {
                        // 触摸向左滑动，表示不打开左边视图，恢复内容视图位置
                        smoothLayerScrollTo(0, 0);
                    }

                } else if (getLayerScrollX() > 0) {
                    // 右边
                    if (velocityTracker.getXVelocity() < 0) {
                        // 触摸向左滑动
                        if (canOpenRight(velocityTracker.getXVelocity(), velocityTracker.getYVelocity())) {
                            // 可以打开右边
                            smoothLayerScrollTo(getHorizontalLayerScrollMax(), getLayerScrollY());
                        } else { // 不可以打开右边视图，恢复内容视图位置
                            smoothLayerScrollTo(0, 0);
                        }
                    } else {
                        // 触摸向右滑动，表示不打开右边视图，恢复内容视图位置
                        smoothLayerScrollTo(0, 0);
                    }
                }
            }
        }

        if (resetTouch) {
            resetTouchStart(event.getX(), event.getY());
        }
        return true;
    }

    private void handleTouchEvent(MotionEvent event) {
        velocityTracker.addMovement(event);
        gestureHelper.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            touching = true;
            resetTouchStart(event.getX(), event.getY());
            if (!layerScroller.isFinished()) {
                layerScroller.abortAnimation(); // 停止滑动
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP ||
                event.getAction() == MotionEvent.ACTION_CANCEL) {
            touching = false;
        }
    }

    private void resetTouchStart(float x, float y) {
        touchScrollStartX = getLayerScrollX();
        touchScrollStartY = getLayerScrollY();
        touchStartX = x;
        touchStartY = y;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        long code = computeInfoCode((long) widthMeasureSpec + heightMeasureSpec);
        if (code == viewInfoCode) {
            // 视图没有发生更改，不需要再次测量，直接使用旧的宽和高
            setMeasuredDimension(getMeasuredWidth(), getMeasuredHeight());
            return;
        }
        viewInfoCode = code;

        // 获取内间距
        int paddingWidth = getPaddingLeft() + getPaddingRight();
        int paddingHeight = getPaddingTop() + getPaddingBottom();
        // 制作自己的测量规格，用于约束子视图
        int smsw = MeasureUtils.makeSelfMeasureSpec(widthMeasureSpec, paddingWidth);
        int smsh = MeasureUtils.makeSelfMeasureSpec(heightMeasureSpec, paddingHeight);
        // 制作自己的测量规格，不受padding影响，用于约束周边视图
        int smswnp = MeasureUtils.makeSelfMeasureSpec(widthMeasureSpec, 0);
        int smshnp = MeasureUtils.makeSelfMeasureSpec(heightMeasureSpec, 0);
        // 内容的大小
        int contentWidth = 0;
        int contentHeight = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) continue;

            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            // 获取外边距
            int marginWidth = lp.leftMargin + lp.rightMargin;
            int marginHeight = lp.topMargin + lp.bottomMargin;
            // 制作子视图的测量规格
            int cmsw, cmsh;
            switch (lp.layer) {
                case LayoutParams.LAYER_LEFT:
                case LayoutParams.LAYER_RIGHT:
                case LayoutParams.LAYER_TOP:
                case LayoutParams.LAYER_BOTTOM:
                    cmsw = MeasureUtils.makeChildMeasureSpec(smswnp, lp.width, marginWidth);
                    cmsh = MeasureUtils.makeChildMeasureSpec(smshnp, lp.height, marginHeight);
                    break;
                default:
                    cmsw = MeasureUtils.makeChildMeasureSpec(smsw, lp.width, marginWidth);
                    cmsh = MeasureUtils.makeChildMeasureSpec(smsh, lp.height, marginHeight);
                    break;
            }
            // 测量子视图
            child.measure(cmsw, cmsh);
            // 子视图实际占用的大小（包括外边距）
            int cw = marginWidth + child.getMeasuredWidth();
            int ch = marginHeight + child.getMeasuredHeight();
            // 选最大的作为内容大小
            if (cw > contentWidth) {
                contentWidth = cw;
            }
            if (ch > contentHeight) {
                contentHeight = ch;
            }
        }
        // 最后的内容大小应该加上内边距
        contentWidth += paddingWidth;
        contentHeight += paddingHeight;
        // 获取当前内容大小对应的视图大小
        int width = MeasureUtils.getMeasuredDimension(contentWidth, widthMeasureSpec);
        int height = MeasureUtils.getMeasuredDimension(contentHeight, heightMeasureSpec);
        // 设置视图大小
        setMeasuredDimension(width, height);

        edgeSize.setEmpty(); // 清空边缘大小
        centerRect.left = getPaddingLeft();
        centerRect.top = getPaddingTop();
        int centerWidth = Math.max(getMeasuredWidth() - getPaddingLeft() - getPaddingRight(), 0);
        int centerHeight = Math.max(getMeasuredHeight() - getPaddingTop() - getPaddingBottom(), 0);
        centerRect.right = centerRect.left + centerWidth;
        centerRect.bottom = centerRect.top + centerHeight;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) continue;

            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            int widthSpace = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            int heightSpace = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
            // 计算周边大小
            switch (lp.layer) {
                case LayoutParams.LAYER_LEFT:
                    if (widthSpace > edgeSize.left) {
                        edgeSize.left = widthSpace;
                    }
                    break;
                case LayoutParams.LAYER_TOP:
                    if (heightSpace > edgeSize.top) {
                        edgeSize.top = heightSpace;
                    }
                    break;
                case LayoutParams.LAYER_RIGHT:
                    if (widthSpace > edgeSize.right) {
                        edgeSize.right = widthSpace;
                    }
                    break;
                case LayoutParams.LAYER_BOTTOM:
                    if (heightSpace > edgeSize.bottom) {
                        edgeSize.bottom = heightSpace;
                    }
                    break;
                case LayoutParams.LAYER_NONE:
                case LayoutParams.LAYER_CENTER:
                default:
                    break;
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        onLayoutChildren();
    }

    /**
     * 布局子View
     */
    protected void onLayoutChildren() {
        // 布局子View
        System.out.println("onLayoutChildren==================");
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) continue;

            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            int gravity = lp.gravity;
            // 默认放在左上角
            if (gravity == Gravity.NO_GRAVITY)
                gravity = Gravity.LEFT | Gravity.TOP;
            // 计算子视图占用的空间
            int widthSpace = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            int heightSpace = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;

            switch (lp.layer) {
                case LayoutParams.LAYER_CENTER: // 放在滑动层中间，受layerScroll位置影响
                    containerRect.set(centerRect);
                    containerRect.offset(-layerScrollX, -layerScrollY);
                    break;

                case LayoutParams.LAYER_LEFT: // 放在滑动层左边，受layerScroll位置影响
                    containerRect.set(centerRect.left - edgeSize.left,
                            centerRect.top, centerRect.left, centerRect.bottom);
                    containerRect.offset(-layerScrollX, -layerScrollY);
                    break;

                case LayoutParams.LAYER_TOP: // 放在滑动层上边，受layerScroll位置影响
                    containerRect.set(centerRect.left, centerRect.top - edgeSize.top,
                            centerRect.right, centerRect.top);
                    containerRect.offset(-layerScrollX, -layerScrollY);
                    break;

                case LayoutParams.LAYER_RIGHT: // 放在滑动层右边，受layerScroll位置影响
                    containerRect.set(centerRect.right, centerRect.top,
                            centerRect.right + edgeSize.right, centerRect.bottom);
                    containerRect.offset(-layerScrollX, -layerScrollY);
                    break;

                case LayoutParams.LAYER_BOTTOM: // 放在滑动层下边，受layerScroll位置影响
                    containerRect.set(centerRect.left, centerRect.bottom,
                            centerRect.right, centerRect.bottom + edgeSize.bottom);
                    containerRect.offset(-layerScrollX, -layerScrollY);
                    break;

                case LayoutParams.LAYER_NONE: // 不滑动层，不受layerScroll位置影响
                default:
                    containerRect.set(centerRect);
                    break;
            }

            // 根据重力倾向，计算出视图位置
            Gravity.apply(gravity, widthSpace, heightSpace, containerRect, outRect);
            System.out.println(String.format("Gravity.apply:%s,%s", containerRect, outRect));
            outRect.left += lp.leftMargin;
            outRect.top += lp.topMargin;
            outRect.right -= lp.rightMargin;
            outRect.bottom -= lp.bottomMargin;
            // 对子View进行布局
            child.layout(outRect.left, outRect.top, outRect.right, outRect.bottom);
        }
    }

    public void updateLayerScroll() {
        removeUpdateLayerScrollRunnable();
        updateLayerScrollRunnable = new Runnable() {
            @Override
            public void run() {
                updateLayerScrollRunnable = null;
                computeLayerScroll();
            }
        };
        postDelayed(updateLayerScrollRunnable, 10);
    }

    protected void computeLayerScroll() {
        if (layerScroller.computeScrollOffset()) {
            int x = layerScroller.getCurrX();
            int y = layerScroller.getCurrY();
//            System.out.println(String.format("computeScroll：x=%d,y=%d", x, y));
            layerScrollTo(x, y);
            updateLayerScroll();
        }
    }

    /**
     * 将滑动层滑动至指定位置，周边滑动位置、状态发生变化
     *
     * @param x 位置X
     * @param y 位置Y
     */
    public void layerScrollTo(int x, int y) {
        int oldX = this.layerScrollX;
        int oldY = this.layerScrollY;
        this.layerScrollX = x;
        this.layerScrollY = y;
//        System.out.println(String.format("layerScrollTo:x=%d,y=%d", x, y));
        requestLayout();

        // 触发对应方法
        onLayerScrollChanged(oldX, oldY, x, y);
        if (null != onLayerScrollChangedListener) {
            onLayerScrollChangedListener.onLayerScrollChanged(this);
        }
    }

    /**
     * 将滑动层滑动至指定位置，动画
     *
     * @param x 位置X
     * @param y 位置Y
     */
    public void smoothLayerScrollTo(int x, int y) {
        if (touching) return; // 触摸滑动中，不支持滑动

        int dx = x - getLayerScrollX();
        int dy = y - getLayerScrollY();
        if (!layerScroller.isFinished()) {
            layerScroller.abortAnimation();
        }
//        System.out.println(String.format("smoothLayerScrollBy:sx=%d,sy=%d,dx=%d,dy=%d", getLayerScrollX(), getLayerScrollY(), dx, dy));
        float size = Math.max(Math.abs(dx), Math.abs(dy));
        int duration;
        if (scrollVelocity > 0) {
            duration = (int) (size / scrollVelocity * 1000);
        } else {
            duration = 100;
        }

        // 最大滑动时间限制
        if (maxScrollTime > 0) {
            if (duration > maxScrollTime) {
                duration = maxScrollTime;
            }
        }

        layerScroller.startScroll(getLayerScrollX(), getLayerScrollY(), dx, dy, duration);
        updateLayerScroll();
    }

    /**
     * 获取滑动层的位置X
     *
     * @return 滑动层的位置X
     */
    public int getLayerScrollX() {
        return layerScrollX;
    }

    /**
     * 获取滑动层的位置Y
     *
     * @return 滑动层的位置Y
     */
    public int getLayerScrollY() {
        return layerScrollY;
    }

    /**
     * 判断是否可以打开左边
     *
     * @param xv 水平方向速度
     * @param yv 垂直方向速度
     * @return true，可以打开左边
     */
    protected boolean canOpenLeft(float xv, float yv) {
        return (null != canOpenEdgeCallback &&
                canOpenEdgeCallback.canOpenLeft(this, xv, yv))
                || getLayerScrollX() <= getHorizontalLayerScrollMin();
    }

    /**
     * 判断是否可以打开右边
     *
     * @param xv 水平方向速度
     * @param yv 垂直方向速度
     * @return true，可以打开右边
     */
    protected boolean canOpenRight(float xv, float yv) {
        return (null != canOpenEdgeCallback &&
                canOpenEdgeCallback.canOpenRight(this, xv, yv))
                || getLayerScrollX() >= getHorizontalLayerScrollMax();
    }

    /**
     * 判断是否可以打开上边
     *
     * @param xv 水平方向速度
     * @param yv 垂直方向速度
     * @return true，可以打开上边
     */
    protected boolean canOpenTop(float xv, float yv) {
        return (null != canOpenEdgeCallback &&
                canOpenEdgeCallback.canOpenTop(this, xv, yv))
                || getLayerScrollY() <= getVerticalLayerScrollMin();
    }

    /**
     * 判断是否可以打开下边
     *
     * @param xv 水平方向速度
     * @param yv 垂直方向速度
     * @return true，可以打开下边
     */
    protected boolean canOpenBottom(float xv, float yv) {
        return (null != canOpenEdgeCallback &&
                canOpenEdgeCallback.canOpenBottom(this, xv, yv))
                || getLayerScrollY() >= getVerticalLayerScrollMax();
    }

    @Override
    public boolean canScrollHorizontally(int direction) {
        if (direction < 0) {
            return getLayerScrollX() > getHorizontalLayerScrollMin();
        } else if (direction > 0) {
            return getLayerScrollX() < getHorizontalLayerScrollMax();
        }
        return false;
    }

    @Override
    public boolean canScrollVertically(int direction) {
        if (direction < 0) {
            return getLayerScrollY() > getVerticalLayerScrollMin();
        } else if (direction > 0) {
            return getLayerScrollY() < getVerticalLayerScrollMax();
        }
        return false;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        attached = true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        System.out.println("DragLayout:onDetachedFromWindow");
        attached = false;
        viewInfoCode = 0;
        afterLayoutRunnableList = null;
    }

    /**
     * 判断滑动层是否可以水平滑动
     *
     * @param direction 方向：负数表示ScrollX值变小的方向；整数表示ScrollX值变大的方向
     * @return true，可以水平滑动
     */
    public boolean canLayerScrollHorizontally(int direction) {
        if (direction < 0) {
            return getLayerScrollX() > getHorizontalLayerScrollMin();
        } else if (direction > 0) {
            return getLayerScrollX() < getHorizontalLayerScrollMax();
        }
        return false;
    }

    /**
     * 判断滑动层是否可以垂直滑动
     *
     * @param direction 方向：负数表示ScrollY值变小的方向；整数表示ScrollY值变大的方向
     * @return true，可以垂直滑动
     */
    public boolean canLayerScrollVertically(int direction) {
        if (direction < 0) {
            return getLayerScrollY() > getVerticalLayerScrollMin();
        } else if (direction > 0) {
            return getLayerScrollY() < getVerticalLayerScrollMax();
        }
        return false;
    }

    /**
     * 判断子视图是否可以垂直滑动
     *
     * @param event     滑动事件
     * @param direction 方向：负数表示ScrollY值变小的方向；整数表示ScrollY值变大的方向
     * @return true，子View可以滑动
     */
    protected boolean canChildrenScrollVertically(MotionEvent event, int direction) {
        for (int i = 0; i < getChildCount(); i++) {
            int index = getChildCount() - 1 - i;
            View child = getChildAt(index);
            if (child.getVisibility() == VISIBLE && child.isEnabled()) {
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
        return null != canChildScrollCallback &&
                canChildScrollCallback.canChildScrollVertically(this, child, direction)
                || child.canScrollVertically(direction);
    }

    /**
     * 判断子视图是否可以水平滑动
     *
     * @param event     滑动事件
     * @param direction 方向：负数表示ScrollX值变小的方向；整数表示ScrollX值变大的方向
     * @return true，子View可以滑动
     */
    protected boolean canChildrenScrollHorizontally(MotionEvent event, int direction) {
        for (int i = 0; i < getChildCount(); i++) {
            int index = getChildCount() - 1 - i;
            View child = getChildAt(index);
            if (child.getVisibility() == VISIBLE && child.isEnabled()) {
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
        return null != canChildScrollCallback &&
                canChildScrollCallback.canChildScrollHorizontally(this, child, direction)
                || child.canScrollHorizontally(direction);
    }

    /**
     * 判断水平方向是否可以滑动到指定位置
     *
     * @param x 位置X
     * @return true，可以滑动到指定位置
     */
    protected boolean canHorizontalScrollTo(int x) {
        if (x < 0) {
            // 左边
            if ((overScroll & OVER_SCROLL_LEFT) == 0) {
                // 左边不能超过滑动范围
                return x >= getHorizontalLayerScrollMin();
            }
        } else if (x > 0) {
            // 右边
            if ((overScroll & OVER_SCROLL_RIGHT) == 0) {
                // 右边不能超过滑动范围
                return x <= getHorizontalLayerScrollMax();
            }
        }
        return true;
    }

    /**
     * 判断垂直方向是否可以滑动到指定位置
     *
     * @param y 位置y
     * @return true，可以滑动到指定位置
     */
    protected boolean canVerticalScrollTo(int y) {
        if (y < 0) {
            // 上边
            if ((overScroll & OVER_SCROLL_TOP) == 0) {
                // 上边不能超过滑动范围
                return y >= getVerticalLayerScrollMin();
            }
        } else if (y > 0) {
            // 下边
            if ((overScroll & OVER_SCROLL_BOTTOM) == 0) {
                // 下边不能超过滑动范围
                return y <= getVerticalLayerScrollMax();
            }
        }
        return true;
    }

    /**
     * 计算视图信息hash code
     *
     * @param offset 开始计算的偏移量
     * @return hash code
     */
    protected long computeInfoCode(long offset) {
        int count = getChildCount();
        long result = offset;
        int index = 0;
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            result = computeHash(result, child.getVisibility(), index++);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            result = computeHash(result, lp.layer, index++);
            result = computeHash(result, lp.gravity, index++);
            result = computeHash(result, lp.width, index++);
            result = computeHash(result, lp.height, index++);
            result = computeHash(result, lp.leftMargin, index++);
            result = computeHash(result, lp.topMargin, index++);
            result = computeHash(result, lp.rightMargin, index++);
            result = computeHash(result, lp.bottomMargin, index++);
        }
        return result;
    }

    private long computeHash(long start, long value, long index) {
        return start + (value << (index % 32));
    }

    /**
     * 获取左边大小
     *
     * @return 大小
     */
    public int getLeftEdgeSize() {
        return edgeSize.left;
    }

    /**
     * 获取水平方向最小滑动位置
     *
     * @return 水平最小滑动位置
     */
    public int getHorizontalLayerScrollMin() {
        return -(edgeSize.left - getPaddingLeft());
    }

    /**
     * 获取上边大小
     *
     * @return 大小
     */
    public int getTopEdgeSize() {
        return edgeSize.top;
    }

    /**
     * 获取垂直方向最小滑动位置
     *
     * @return 垂直最小滑动位置
     */
    public int getVerticalLayerScrollMin() {
        return -(edgeSize.top - getPaddingTop());
    }

    /**
     * 获取右边大小
     *
     * @return 大小
     */
    public int getRightEdgeSize() {
        return edgeSize.right;
    }

    /**
     * 获取水平方向最大滑动位置
     *
     * @return 水平最大滑动位置
     */
    public int getHorizontalLayerScrollMax() {
        return edgeSize.right - getPaddingRight();
    }

    /**
     * 获取下边大小
     *
     * @return 大小
     */
    public int getBottomEdgeSize() {
        return edgeSize.bottom;
    }

    /**
     * 获取垂直方向最大滑动位置
     *
     * @return 垂直最大滑动位置
     */
    public int getVerticalLayerScrollMax() {
        return edgeSize.bottom - getPaddingBottom();
    }

    /**
     * 获取中间内容的范围
     *
     * @return out，中间内容范围
     */
    public Rect getCenterRect(Rect out) {
        if (null == out) {
            out = new Rect();
        }
        out.set(centerRect);
        return out;
    }

    public OnLayerScrollChangedListener getOnLayerScrollChangedListener() {
        return onLayerScrollChangedListener;
    }

    public void setOnLayerScrollChangedListener(OnLayerScrollChangedListener onLayerScrollChangedListener) {
        this.onLayerScrollChangedListener = onLayerScrollChangedListener;
    }

    /**
     * 层滑动发生更改
     *
     * @param oldX 旧X
     * @param oldY 旧Y
     * @param newX 新X
     * @param newY 新Y
     */
    protected void onLayerScrollChanged(int oldX, int oldY, int newX, int newY) {
    }

    public CanOpenEdgeCallback getCanOpenEdgeCallback() {
        return canOpenEdgeCallback;
    }

    public void setCanOpenEdgeCallback(CanOpenEdgeCallback canOpenEdgeCallback) {
        this.canOpenEdgeCallback = canOpenEdgeCallback;
    }

    /**
     * 设置边缘是否可以超出滑动范围
     *
     * @param overScroll 是否可以超出活动范围{@link DragLayout#OVER_SCROLL_NONE}
     *                   {@link #OVER_SCROLL_LEFT OVER_SCROLL_LEFT}
     *                   {@link #OVER_SCROLL_TOP OVER_SCROLL_TOP}
     *                   {@link #OVER_SCROLL_RIGHT OVER_SCROLL_RIGHT}
     *                   {@link #OVER_SCROLL_BOTTOM OVER_SCROLL_BOTTOM}
     *                   {@link #OVER_SCROLL_ALL OVER_SCROLL_ALL}
     */
    public void setOverScroll(int overScroll) {
        this.overScroll = overScroll;
        requestLayout();
    }

    private void removeUpdateLayerScrollRunnable() {
        if (null != updateLayerScrollRunnable) {
            removeCallbacks(updateLayerScrollRunnable);
            updateLayerScrollRunnable = null;
        }
    }

    public CanChildScrollCallback getCanChildScrollCallback() {
        return canChildScrollCallback;
    }

    public void setCanChildScrollCallback(CanChildScrollCallback canChildScrollCallback) {
        this.canChildScrollCallback = canChildScrollCallback;
    }

    /**
     * 打开左边
     *
     * @param smooth 是否有动画
     */
    public void openLeft(final boolean smooth) {
        postAfterLayout(new Runnable() {
            @Override
            public void run() {
                if (smooth) {
                    smoothLayerScrollTo(getHorizontalLayerScrollMin(), 0);
                } else {
                    if (!layerScroller.isFinished()) {
                        layerScroller.abortAnimation();
                    }
                    layerScrollTo(getHorizontalLayerScrollMin(), 0);
                }
            }
        });
    }

    /**
     * 打开上边
     *
     * @param smooth 是否有动画
     */
    public void openTop(final boolean smooth) {
        postAfterLayout(new Runnable() {
            @Override
            public void run() {
                if (smooth) {
                    smoothLayerScrollTo(0, getVerticalLayerScrollMin());
                } else {
                    if (!layerScroller.isFinished()) {
                        layerScroller.abortAnimation();
                    }
                    layerScrollTo(0, getVerticalLayerScrollMin());
                }
            }
        });
    }

    /**
     * 打开右边
     *
     * @param smooth 是否有动画
     */
    public void openRight(final boolean smooth) {
        postAfterLayout(new Runnable() {
            @Override
            public void run() {
                if (smooth) {
                    smoothLayerScrollTo(getHorizontalLayerScrollMax(), 0);
                } else {
                    if (!layerScroller.isFinished()) {
                        layerScroller.abortAnimation();
                    }
                    layerScrollTo(getHorizontalLayerScrollMax(), 0);
                }
            }
        });
    }

    /**
     * 打开下边
     *
     * @param smooth 是否有动画
     */
    public void openBottom(final boolean smooth) {
        postAfterLayout(new Runnable() {
            @Override
            public void run() {
                if (smooth) {
                    smoothLayerScrollTo(0, getVerticalLayerScrollMax());
                } else {
                    if (!layerScroller.isFinished()) {
                        layerScroller.abortAnimation();
                    }
                    layerScrollTo(0, getVerticalLayerScrollMax());
                }
            }
        });
    }

    /**
     * 关闭边缘
     *
     * @param smooth 是否有动画
     */
    public void closeEdge(final boolean smooth) {
        postAfterLayout(new Runnable() {
            @Override
            public void run() {
                if (smooth) {
                    smoothLayerScrollTo(0, 0);
                } else {
                    if (!layerScroller.isFinished()) {
                        layerScroller.abortAnimation();
                    }
                    layerScrollTo(0, 0);
                }
            }
        });
    }

    /**
     * 在layout之后执行任务
     *
     * @param runnable 任务
     */
    public void postAfterLayout(Runnable runnable) {
        if (attached && !isLayoutRequested()) {
            runnable.run();
        } else {
            if (null == afterLayoutRunnableList) {
                afterLayoutRunnableList = new LinkedList<>();
            }
            afterLayoutRunnableList.addLast(runnable);
        }
    }

    private void releaseTouch() {
        velocityTracker.computeCurrentVelocity(1000);
        if (canOpenLeft(velocityTracker.getXVelocity(), velocityTracker.getYVelocity())) {
            // 可以打开左边
            smoothLayerScrollTo(getHorizontalLayerScrollMin(), getLayerScrollY());

        } else if (canOpenTop(velocityTracker.getXVelocity(), velocityTracker.getYVelocity())) {
            // 可以打开上边
            smoothLayerScrollTo(getLayerScrollX(), getVerticalLayerScrollMin());

        } else if (canOpenRight(velocityTracker.getXVelocity(), velocityTracker.getYVelocity())) {
            // 可以打开右边
            smoothLayerScrollTo(getHorizontalLayerScrollMax(), getLayerScrollY());

        } else if (canOpenBottom(velocityTracker.getXVelocity(), velocityTracker.getYVelocity())) {
            // 可以打开下边
            smoothLayerScrollTo(getLayerScrollX(), getVerticalLayerScrollMax());

        } else {
            smoothLayerScrollTo(0, 0);
        }
    }

    public int getMaxScrollTime() {
        return maxScrollTime;
    }

    public void setMaxScrollTime(int maxScrollTime) {
        this.maxScrollTime = maxScrollTime;
    }

    public static class LayoutParams extends MarginLayoutParams {

        /**
         * 层：无，不受滑动影响，效果相当于将视图放在FrameLayout中
         */
        public static final int LAYER_NONE = 0;
        /**
         * 层：中间，表示放置在布局中间滑动层
         */
        public static final int LAYER_CENTER = 1;
        /**
         * 层：左边，表示放置在布局左边滑动层
         */
        public static final int LAYER_LEFT = 2;
        /**
         * 层：上面，表示放置在布局上面滑动层
         */
        public static final int LAYER_TOP = 3;
        /**
         * 层：中间，表示放置在布局右边滑动层
         */
        public static final int LAYER_RIGHT = 4;
        /**
         * 层：中间，表示放置在布局下面滑动层
         */
        public static final int LAYER_BOTTOM = 5;

        /**
         * 表示视图位于的层
         */
        public int layer = LAYER_NONE;
        public int gravity;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            TypedArray ta = c.obtainStyledAttributes(attrs, R.styleable.DragLayout_Layout);
            this.layer = ta.getInt(R.styleable.DragLayout_Layout_layout_layer, LAYER_NONE);
            this.gravity = ta.getInt(R.styleable.DragLayout_Layout_android_layout_gravity, Gravity.NO_GRAVITY);
            ta.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(LayoutParams source) {
            super((ViewGroup.MarginLayoutParams) source);
            this.gravity = source.gravity;
            this.layer = source.layer;
        }

        @TargetApi(Build.VERSION_CODES.KITKAT)
        public LayoutParams(FrameLayout.LayoutParams source) {
            super(source);
        }
    }

    /**
     * 层滑动更改监听器
     */
    public interface OnLayerScrollChangedListener {

        void onLayerScrollChanged(DragLayout dragLayout);
    }

    /**
     * 是否可以打开边缘回调
     */
    public interface CanOpenEdgeCallback {

        /**
         * 判断是否可以打开左边
         *
         * @param view 视图
         * @param xv   水平方向速度
         * @param yv   垂直方向速度
         * @return true，可以打开左边
         */
        boolean canOpenLeft(DragLayout view, float xv, float yv);

        /**
         * 判断是否可以打开右边
         *
         * @param view 视图
         * @param xv   水平方向速度
         * @param yv   垂直方向速度
         * @return true，可以打开右边
         */
        boolean canOpenRight(DragLayout view, float xv, float yv);

        /**
         * 判断是否可以打开上边
         *
         * @param view 视图
         * @param xv   水平方向速度
         * @param yv   垂直方向速度
         * @return true，可以打开上边
         */
        boolean canOpenTop(DragLayout view, float xv, float yv);

        /**
         * 判断是否可以打开下边
         *
         * @param view 视图
         * @param xv   水平方向速度
         * @param yv   垂直方向速度
         * @return true，可以打开下边
         */
        boolean canOpenBottom(DragLayout view, float xv, float yv);
    }

    public abstract static class SimpleCanOpenEdgeCallback implements CanOpenEdgeCallback {

        @Override
        public boolean canOpenLeft(DragLayout view, float xv, float yv) {
            return false;
        }

        @Override
        public boolean canOpenRight(DragLayout view, float xv, float yv) {
            return false;
        }

        @Override
        public boolean canOpenTop(DragLayout view, float xv, float yv) {
            return false;
        }

        @Override
        public boolean canOpenBottom(DragLayout view, float xv, float yv) {
            return false;
        }
    }

    public interface CanChildScrollCallback {

        boolean canChildScrollVertically(DragLayout view, View child, int direction);

        boolean canChildScrollHorizontally(DragLayout view, View child, int direction);
    }
}