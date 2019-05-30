package com.fta.skr.testmethod.active;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.fta.skr.testmethod.config.TagConfig;

public class MdmAdminReceiver extends DeviceAdminReceiver {

    private static final String TAG = TagConfig.TAG;

    @Override
    public void onEnabled(Context context, Intent intent) {
        super.onEnabled(context, intent);
        Log.i(TAG, "MdmAdminReceiver-onEnabled: ");
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        super.onDisabled(context, intent);
        Log.i(TAG, "MdmAdminReceiver-onDisabled: ");
    }
}
