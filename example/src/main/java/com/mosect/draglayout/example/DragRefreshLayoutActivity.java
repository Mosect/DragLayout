package com.mosect.draglayout.example;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.mosect.draglayout.entity.ListItem;
import com.mosect.draglayout.entity.PageEntity;
import com.mosect.draglayout.lib.DragRefreshLayout;
import com.mosect.draglayout.model.ListDataModel;
import com.mosect.draglayout.util.WorkCallback;
import com.mosect.draglayout.util.WorkRunnable;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class DragRefreshLayoutActivity extends AppCompatActivity {

    private ListAdapter contentAdapter;
    private TextView tvHeader;
    private TextView tvFooter;
    private DragRefreshLayout drlContent;
    private int currentPageNumber = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drefresh);

        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        tvHeader = findViewById(R.id.tv_header);
        tvFooter = findViewById(R.id.tv_footer);

        // 初始化列表视图
        RecyclerView rvContent = findViewById(R.id.rv_content);
        contentAdapter = new ListAdapter();
        contentAdapter.initWith(rvContent);

        drlContent = findViewById(R.id.drl_content);
        drlContent.setMaxScrollTime(350);
        drlContent.setOnEdgeStateChangedListener(new DragRefreshLayout.OnEdgeStateChangedListener() {
            @Override
            public void onEdgeStateChanged(DragRefreshLayout view, Rect oldState, Rect state) {
                if (oldState.top != state.top) {
                    changeTopState(state.top);
                    if (state.top == DragRefreshLayout.EDGE_STATE_LOADING) {
                        // 刷新数据
                        loadPage(1, true);
                    }
                }
                if (oldState.bottom != state.bottom) {
                    changeBottomState(state.bottom);
                    if (state.bottom == DragRefreshLayout.EDGE_STATE_LOADING) {
                        // 加载下一页
                        loadPage(currentPageNumber + 1, false);
                    }
                }
            }
        });
        drlContent.setEdgeState(
                DragRefreshLayout.EDGE_STATE_DISABLE,
                DragRefreshLayout.EDGE_STATE_NORMAL,
                DragRefreshLayout.EDGE_STATE_DISABLE,
                DragRefreshLayout.EDGE_STATE_NONE
        );
        changeTopState(drlContent.getEdgeState().top);
        changeBottomState(drlContent.getEdgeState().bottom);

        refresh(); // 刷新数据
    }

    /**
     * 刷新数据
     */
    private void refresh() {
        Rect state = drlContent.getEdgeState();
        state.top = DragRefreshLayout.EDGE_STATE_LOADING;
        state.bottom = DragRefreshLayout.EDGE_STATE_DISABLE;
        drlContent.setEdgeState(state);
        drlContent.openTop(true); // 打开上边
    }

    private void changeTopState(int state) {
        switch (state) {
            case DragRefreshLayout.EDGE_STATE_NONE:
                tvHeader.setText(R.string.top_none);
                break;
            case DragRefreshLayout.EDGE_STATE_NORMAL:
                tvHeader.setText(R.string.top_normal);
                break;
            case DragRefreshLayout.EDGE_STATE_FINISH:
                // 成功或失败
                // success or failed
                tvHeader.setText(R.string.top_finish);
                break;
            case DragRefreshLayout.EDGE_STATE_READY:
                tvHeader.setText(R.string.top_ready);
                break;
            case DragRefreshLayout.EDGE_STATE_LOADING:
                tvHeader.setText(R.string.top_loading);
                break;
        }
    }

    private void changeBottomState(int state) {
        switch (state) {
            case DragRefreshLayout.EDGE_STATE_NONE:
                tvFooter.setText(R.string.bottom_none);
                break;
            case DragRefreshLayout.EDGE_STATE_NORMAL:
                tvFooter.setText(R.string.bottom_normal);
                break;
            case DragRefreshLayout.EDGE_STATE_FINISH:
                // 成功或失败
                // success or failed
                tvFooter.setText(R.string.bottom_finish);
                break;
            case DragRefreshLayout.EDGE_STATE_READY:
                tvFooter.setText(R.string.bottom_ready);
                break;
            case DragRefreshLayout.EDGE_STATE_LOADING:
                tvFooter.setText(R.string.bottom_loading);
                break;
        }
    }

    private void loadPage(final int page, final boolean refresh) {
        Observable<PageEntity<ListItem>> observable = Observable.create(new WorkRunnable<PageEntity<ListItem>>() {
            @Override
            protected PageEntity<ListItem> doWork() throws Throwable {
                return ListDataModel.getInstance().loadPage(page);
            }
        });
        observable.observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new WorkCallback<PageEntity<ListItem>>() {
                    @Override
                    protected void onSuccess(PageEntity<ListItem> result) {
                        onLoadSuccess(result, refresh);
                    }
                });
    }

    private void onLoadSuccess(PageEntity<ListItem> pageEntity, boolean refresh) {

        // 添加数据
        currentPageNumber = pageEntity.getPageNumber();
        if (refresh) {
            contentAdapter.clear();
        }
        if (null != pageEntity.getList()) {
            contentAdapter.addAll(pageEntity.getList());
        }

        // 计算页面相关数据
        int count = null == pageEntity.getList() ? 0 : pageEntity.getList().size();
        boolean hasNextPage = (pageEntity.getPageNumber() - 1) * pageEntity.getPageSize() + count
                < pageEntity.getTotal();

        // 设置视图状态
        Rect state = drlContent.getEdgeState();
        if (refresh) {
            state.top = DragRefreshLayout.EDGE_STATE_FINISH;
            state.bottom = hasNextPage ? DragRefreshLayout.EDGE_STATE_NORMAL
                    : DragRefreshLayout.EDGE_STATE_NONE;
        } else {
            state.top = DragRefreshLayout.EDGE_STATE_NORMAL;
            state.bottom = hasNextPage ? DragRefreshLayout.EDGE_STATE_NORMAL :
                    DragRefreshLayout.EDGE_STATE_NONE;
        }
        drlContent.setEdgeState(state);

        // 关闭周边
        if (drlContent.getLayerScrollY() < 0) {
            drlContent.postDelayed(new Runnable() {
                @Override
                public void run() {
                    drlContent.closeEdge(true);
                }
            }, 1000L);
        } else if (drlContent.getLayerScrollY() > 0) {
            drlContent.closeEdge(true);
        }
    }
}
