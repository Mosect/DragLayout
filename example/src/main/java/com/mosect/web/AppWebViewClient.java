package com.mosect.web;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

public class AppWebViewClient extends WebViewClient {

    private WebHandler webHandler;
    private boolean finalUrl = false;

    public AppWebViewClient(@NonNull WebHandler webHandler) {
        this.webHandler = webHandler;
    }

    @Override
    public void onPageStarted(WebView webView, String s, Bitmap bitmap) {
        super.onPageStarted(webView, s, bitmap);
        finalUrl = true;
    }

    @Override
    public void onPageFinished(WebView webView, String s) {
        super.onPageFinished(webView, s);
        if (finalUrl) {
            webHandler.setViewProgress(-1);
        }
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView webView, String url) {
//        openUrl(url, "GET", null);
        finalUrl = false;
        webView.loadUrl(url);
        return true;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView webView, WebResourceRequest webResourceRequest) {
//        openUrl(webResourceRequest.getUrl().toString(), webResourceRequest.getMethod(),
//                webResourceRequest.getRequestHeaders());
        finalUrl = false;
        webView.loadUrl(webResourceRequest.getUrl().toString(), webResourceRequest.getRequestHeaders());
        return true;
    }

    //    private void openUrl(String url, String method, Map<String, String> headers) {
//        Bundle args = new Bundle();
//        args.putString("url", url);
//        args.putString("method", method);
//        if (null != headers) {
//            args.putSerializable("headers", new HashMap<>(headers));
//        }
//        BaseFragment.start(viewContext.getContext(), WebFragment.class, args);
//    }
}
