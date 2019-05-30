package com.fta.skr.testmethod;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.LauncherApps;
import android.os.Build;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.fta.skr.testmethod.config.TagConfig;
import com.fta.skr.testmethod.receiver.MyBroadcastReceiver;

public class BroadcastActivity extends AppCompatActivity {

    private static final String TAG = TagConfig.TAG;

    private MyBroadcastReceiver myBroadcastReceiver;

    private Context mContext;

    private LauncherApps launcherApps;
    private MyCallback myCallback;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_broadcast);
        mContext = this;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            launcherApps = ((LauncherApps) mContext.getSystemService(Context.LAUNCHER_APPS_SERVICE));
        }

    }

    public void registerBro(View view) {
        if (launcherApps != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                myCallback = new MyCallback();
                Log.i(TAG, "BroadcastActivity-registerBro: 注册广播");
                launcherApps.registerCallback(myCallback);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static class MyCallback extends LauncherApps.Callback {


        /**
         * 应用安装
         * @param packageName
         * @param user
         */
        @Override
        public void onPackageRemoved(String packageName, UserHandle user) {
            Log.i(TAG, "MyCallback-onPackageRemoved: packageName=" + packageName);
        }

        /**
         * 应用卸载
         * @param packageName
         * @param user
         */
        @Override
        public void onPackageAdded(String packageName, UserHandle user) {
            Log.i(TAG, "MyCallback-onPackageAdded: packageName=" + packageName);
        }


        /**
         * 表示在指定的配置文件中修改了包。 *例如，当更新软件包或启用或禁用*一个或多个组件时，可能会发生这种情况。
         * @param packageName
         * @param user
         */
        @Override
        public void onPackageChanged(String packageName, UserHandle user) {
            Log.i(TAG, "MyCallback-onPackageChanged: packageName=" + packageName);
        }


        /**
         * 表示一个或多个包可用。例如，当可移动存储卡重新出现时，可能会发生这种情况。
         * @param packageNames
         * @param user
         * @param replacing
         */
        @Override
        public void onPackagesAvailable(String[] packageNames, UserHandle user, boolean replacing) {
            Log.i(TAG, "MyCallback-onPackagesAvailable: ");
        }


        /**
         * 表示一个或多个软件包已不可用。例如，删除可移动存储卡时可能会发生这种情况。
         * @param packageNames
         * @param user
         * @param replacing
         */
        @Override
        public void onPackagesUnavailable(String[] packageNames, UserHandle user, boolean replacing) {
            Log.i(TAG, "MyCallback-onPackagesUnavailable: ");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (launcherApps != null && myCallback != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                launcherApps.unregisterCallback(myCallback);
            }
        }
    }
}
