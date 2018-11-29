package com.mosect.draglayout.example;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.mosect.web.AppWebChromeClient;
import com.mosect.web.AppWebView;
import com.mosect.web.AppWebViewClient;
import com.mosect.web.WebHandler;

public class WebFragment extends Fragment implements WebHandler {

    public static WebFragment newInstance(String url) {
        WebFragment fragment = new WebFragment();
        Bundle args = new Bundle();
        args.putString("url", url);
        fragment.setArguments(args);
        return fragment;
    }

    private String url;
    private AppWebView wvContent;
    private ProgressBar pbWeb;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (null == savedInstanceState) {
            if (null != getArguments()) {
                url = getArguments().getString("url");
            }
        } else {
            url = savedInstanceState.getString("url");
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("url", url);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_web, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        wvContent = view.findViewById(R.id.wv_content);
        pbWeb = view.findViewById(R.id.pb_web);
        wvContent.setWebChromeClient(new AppWebChromeClient(this));
        wvContent.setWebViewClient(new AppWebViewClient(this));
        if (!TextUtils.isEmpty(url)) {
            wvContent.loadUrl(url);
        }
    }

    @Override
    public void setViewProgress(int progress) {
        if (null != pbWeb) {
            if (progress < 0) {
                pbWeb.setProgress(0);
            } else {
                pbWeb.setProgress(progress);
            }
        }
    }

    @Override
    public void setViewTitle(CharSequence title) {

    }

    public AppWebView getWebView() {
        return wvContent;
    }
}
