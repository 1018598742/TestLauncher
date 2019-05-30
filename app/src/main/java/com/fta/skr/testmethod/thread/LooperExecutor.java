package com.fta.skr.testmethod.thread;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 自定义线程池
 */
public class LooperExecutor extends AbstractExecutorService{

    private final Handler mHandler;

    public LooperExecutor(Looper looper) {
        mHandler = new Handler(looper);
    }


    @Override
    public void execute(@NonNull Runnable runnable) {
        if (mHandler.getLooper() == Looper.myLooper()) {
            runnable.run();
        } else {
            mHandler.post(runnable);
        }
    }

    @Override
    public void shutdown() {
        throw new UnsupportedOperationException();
    }

    @NonNull
    @Override
    public List<Runnable> shutdownNow() {
        throw new UnsupportedOperationException();
    }


    @Override
    public boolean isShutdown() {
        return false;
    }


    @Override
    public boolean isTerminated() {
        return false;
    }


    @Override
    public boolean awaitTermination(long timeout, @NonNull TimeUnit unit) throws InterruptedException {
        throw new UnsupportedOperationException();
    }

}
