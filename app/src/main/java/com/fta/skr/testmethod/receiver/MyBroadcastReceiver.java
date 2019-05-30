package com.fta.skr.testmethod.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.fta.skr.testmethod.config.TagConfig;

public class MyBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = TagConfig.TAG;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.i(TAG, "MyBroadcastReceiver-onReceive: " + action);
    }
}
