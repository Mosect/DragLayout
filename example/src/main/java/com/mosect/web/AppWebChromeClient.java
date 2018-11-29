package com.mosect.web;

import android.support.annotation.NonNull;

import com.tencent.smtt.export.external.interfaces.JsResult;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebView;

public class AppWebChromeClient extends WebChromeClient {

    private WebHandler webHandler;
    private boolean setTitle = true;

    public AppWebChromeClient(@NonNull WebHandler webHandler) {
        this.webHandler = webHandler;
    }

    @Override
    public void onProgressChanged(WebView webView, int progress) {
        webHandler.setViewProgress(progress);
    }

    @Override
    public boolean onJsAlert(WebView webView, String s, String s1, JsResult jsResult) {
        return super.onJsAlert(webView, s, s1, jsResult);
    }

    @Override
    public boolean onJsConfirm(WebView webView, String s, String s1, JsResult jsResult) {
        return super.onJsConfirm(webView, s, s1, jsResult);
    }

    @Override
    public void onReceivedTitle(WebView webView, String s) {
        if (isSetTitle()) {
            webHandler.setViewTitle(s);
        }
    }

    public boolean isSetTitle() {
        return setTitle;
    }

    public AppWebChromeClient setSetTitle(boolean setTitle) {
        this.setTitle = setTitle;
        return this;
    }
}
