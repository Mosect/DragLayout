package com.mosect.draglayout.example;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mosect.draglayout.lib.DragLayout;
import com.mosect.draglayout.lib.FlowLayout;
import com.mosect.web.AppWebView;

public class FlowLayoutActivity extends AppCompatActivity {

    private ViewPager vpContent;
    private WebFragment[] fragments;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int type = getIntent().getIntExtra("type", 1);
        CharSequence title = null;
        switch (type) {
            case 1:
                setContentView(R.layout.activity_flow);
                title = getString(R.string.btn_flow);
                break;
            case 2:
                setContentView(R.layout.activity_flow2);
                title = getString(R.string.btn_flow2);
                break;
        }

        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        final TextView tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText(title);

        FlowLayout flowLayout = findViewById(R.id.ly_flow);
        // 默认打开上边视图
        flowLayout.openTop(false);
        // 根据滑动改变标题透明度
        flowLayout.setOnLayerScrollChangedListener(new DragLayout.OnLayerScrollChangedListener() {
            @Override
            public void onLayerScrollChanged(DragLayout dragLayout) {
                float y = dragLayout.getLayerScrollY();
                if (dragLayout.getVerticalLayerScrollMin() != 0) {
                    float alpha = 1 - y / dragLayout.getVerticalLayerScrollMin();
                    if (alpha < 0) alpha = 0;
                    else if (alpha > 1) alpha = 1;
                    tvTitle.setAlpha(alpha);
                } else {
                    tvTitle.setAlpha(0);
                }
            }
        });
        // 设置子视图是否可以滑动，主要是WebView
        flowLayout.setCanChildScrollCallback(new DragLayout.CanChildScrollCallback() {
            @Override
            public boolean canChildScrollVertically(DragLayout view, View child, int direction) {
                AppWebView webView = fragments[vpContent.getCurrentItem()].getWebView();
                return null != webView && webView.canScrollVertically(direction);
            }

            @Override
            public boolean canChildScrollHorizontally(DragLayout view, View child, int direction) {
                return false;
            }
        });

        ImageView ivHeader = findViewById(R.id.iv_header);
        Glide.with(this)
                .asBitmap()
                .load("http://www.mosect.com:7520/pic1.jpg")
                .into(ivHeader);

        vpContent = findViewById(R.id.vp_content);
        TabLayout tabLayout = findViewById(R.id.ly_tab);
        final String[] tabTitles = {
                "BAIDU", "BING", "GOOGLE",
        };
        fragments = new WebFragment[]{
                WebFragment.newInstance("http://www.baidu.com"),
                WebFragment.newInstance("http://www.bing.com"),
                WebFragment.newInstance("http://www.google.com"),
        };
        vpContent.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int i) {
                return fragments[i];
            }

            @Override
            public int getCount() {
                return fragments.length;
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                return tabTitles[position];
            }
        });
        tabLayout.setupWithViewPager(vpContent, true);
    }
}
