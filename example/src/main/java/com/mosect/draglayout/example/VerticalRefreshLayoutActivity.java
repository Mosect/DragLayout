package com.mosect.draglayout.example;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.mosect.draglayout.entity.ListItem;
import com.mosect.draglayout.entity.PageEntity;
import com.mosect.draglayout.lib.DragRefreshLayout;
import com.mosect.draglayout.lib.VerticalRefreshLayout;
import com.mosect.draglayout.model.ListDataModel;
import com.mosect.draglayout.util.WorkCallback;
import com.mosect.draglayout.util.WorkRunnable;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class VerticalRefreshLayoutActivity
        extends AppCompatActivity
        implements VerticalRefreshLayout.LoadCallback,
        VerticalRefreshLayout.OnLoadStateChangedListener {

    private ListAdapter contentAdapter;
    private TextView tvHeader;
    private TextView tvFooter;
    private VerticalRefreshLayout vrlContent;
    private int currentPageNumber = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vrefresh);

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

        vrlContent = findViewById(R.id.vrl_content);
        vrlContent.setLoadCallback(this);
        vrlContent.setOnLoadStateChangedListener(this);
        updateTopState();
        updateBottomState();

        // 执行刷新
        vrlContent.postRefresh(true);
    }

    @Override
    public void onRefresh(VerticalRefreshLayout view) {
        System.out.println("VerticalRefreshLayoutActivity:onRefresh");
        loadPage(1, true);
    }

    @Override
    public void onLoadMore(VerticalRefreshLayout view) {
        System.out.println("VerticalRefreshLayoutActivity:onLoadMore");
        loadPage(currentPageNumber + 1, false);
    }

    @Override
    public void onRefreshStateChanged(VerticalRefreshLayout view, int oldState, int state) {
        updateTopState();
    }

    @Override
    public void onLoadMoreStateChanged(VerticalRefreshLayout view, int oldState, int state) {
        updateBottomState();
    }

    private void updateTopState() {
        int state = vrlContent.getEdgeState().top;
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

    private void updateBottomState() {
        int state = vrlContent.getEdgeState().bottom;
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

    /**
     * 加载分页数据
     *
     * @param page    页数
     * @param refresh 是否为刷新操作
     */
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
                        loadPageSuccess(result, refresh);
                    }
                });
    }

    private void loadPageSuccess(PageEntity<ListItem> data, boolean refresh) {
        if (null != data) {
            currentPageNumber = data.getPageNumber();
        }

        // 添加数据
        if (refresh) {
            contentAdapter.clear();
        }
        if (null != data && null != data.getList()) {
            contentAdapter.addAll(data.getList());
        }

        int count = null != data && null != data.getList() ? data.getList().size() : 0;
        boolean hasNextPage = null != data && (data.getPageNumber() - 1) * data.getPageSize() +
                count < data.getTotal();
        vrlContent.finishLoad(hasNextPage);
        vrlContent.postDelayed(new Runnable() {
            @Override
            public void run() {
                vrlContent.closeEdge(true);
            }
        }, 500);
    }
}
