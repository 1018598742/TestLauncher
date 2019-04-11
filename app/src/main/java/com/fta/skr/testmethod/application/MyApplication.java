package com.fta.skr.testmethod.application;

import android.app.Application;
import android.util.Log;

import com.fta.skr.testmethod.config.TagConfig;

public class MyApplication extends Application {

    private static final String TAG = TagConfig.TAG;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "MyApplication-onCreate: ");
    }
}
