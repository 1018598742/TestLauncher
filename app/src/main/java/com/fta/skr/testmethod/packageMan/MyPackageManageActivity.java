package com.fta.skr.testmethod.packageMan;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.fta.skr.testmethod.R;
import com.fta.skr.testmethod.config.TagConfig;

import java.io.File;

public class MyPackageManageActivity extends AppCompatActivity {

    private static final String TAG = TagConfig.TAG;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pack);
    }

    public void packMan(View view) {
        PackageManager packageManager = getPackageManager();
        File file = new File(Environment.getExternalStorageDirectory(), "应用信息管家 (2)_1001_jiagu.apk");
        if (file.exists()) {
            String absolutePath = file.getAbsolutePath();
            Log.i(TAG, "MyPackageManageActivity-packMan: 文件位置：" + absolutePath);
            PackageInfo packageArchiveInfo = packageManager.getPackageArchiveInfo(absolutePath,  PackageManager.GET_SIGNATURES);
            Log.i(TAG, "MyPackageManageActivity-packMan: " + (packageArchiveInfo != null ? "is not null" : "is null"));
            if (packageArchiveInfo != null) {
                Signature[] signatures = packageArchiveInfo.signatures;
                Log.i(TAG, "MyPackageManageActivity-packMan: signatures=" + (signatures != null ? "is not null" : "is null"));
            }
        }

    }

    public void packMan2(View view) {
        PackageManager packageManager = getPackageManager();
        File file = new File(Environment.getExternalStorageDirectory(), "应用信息管家.apk");
        if (file.exists()) {
            String absolutePath = file.getAbsolutePath();
            Log.i(TAG, "MyPackageManageActivity-packMan2: 文件位置：" + absolutePath);
            PackageInfo packageArchiveInfo = packageManager.getPackageArchiveInfo(absolutePath, PackageManager.GET_SIGNATURES);
            Log.i(TAG, "MyPackageManageActivity-packMan2: " + (packageArchiveInfo != null ? "is not null" : "is null"));
            if (packageArchiveInfo != null) {
                Signature[] signatures = packageArchiveInfo.signatures;
                Log.i(TAG, "MyPackageManageActivity-packMan2: signatures=" + (signatures != null ? "is not null" : "is null"));
            }
        }
    }
}
