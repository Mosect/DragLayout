package com.mosect.draglayout.util;

import android.support.annotation.CallSuper;

import rx.Subscriber;

public abstract class WorkCallback<T> extends Subscriber<T> {

    @CallSuper
    @Override
    public void onCompleted() {
        onFinish();
    }

    @CallSuper
    @Override
    public void onError(Throwable e) {
        onFailed(e);
        onFinish();
    }

    @CallSuper
    @Override
    public void onNext(T t) {
        onSuccess(t);
    }

    /**
     * 成功
     *
     * @param result 结果
     */
    protected void onSuccess(T result) {
    }

    /**
     * 失败
     *
     * @param e 异常
     */
    protected void onFailed(Throwable e) {
    }

    /**
     * 完成
     */
    protected void onFinish() {
    }
}
