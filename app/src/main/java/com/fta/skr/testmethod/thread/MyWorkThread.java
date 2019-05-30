package com.fta.skr.testmethod.thread;

import android.os.HandlerThread;
import android.os.Looper;

public class MyWorkThread {

    static final HandlerThread sWorkerThread = new HandlerThread("launcher-loader");

    static {
        sWorkerThread.start();
    }


    public static Looper getWorkerLooper() {
        return sWorkerThread.getLooper();
    }

}
