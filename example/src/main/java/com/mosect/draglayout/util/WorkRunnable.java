package com.mosect.draglayout.util;

import rx.Observable;
import rx.Subscriber;

public abstract class WorkRunnable<T> implements Observable.OnSubscribe<T> {

    @Override
    public void call(Subscriber<? super T> subscriber) {
        try {
            T result = doWork();
            subscriber.onNext(result);
        } catch (Throwable throwable) {
            subscriber.onError(throwable);
        }
        subscriber.onCompleted();
    }

    protected abstract T doWork() throws Throwable;
}
