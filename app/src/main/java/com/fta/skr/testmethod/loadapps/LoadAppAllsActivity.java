package com.fta.skr.testmethod.loadapps;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.fta.skr.testmethod.R;
import com.fta.skr.testmethod.config.TagConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fta
 * on 2019/4/19
 */
public class LoadAppAllsActivity extends AppCompatActivity {

    private static final String TAG = TagConfig.TAG;
    private Context mContext;

    private UserManagerCompat mUserManager;

    private LauncherAppsCompat mLauncherApps;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loadallapps);
        mContext = this;

        mLauncherApps = LauncherAppsCompat.getInstance(mContext);
        mUserManager = UserManagerCompat.getInstance(mContext);

    }

    public void loadAllApps(View view) {
        final List<UserHandle> profiles = mUserManager.getUserProfiles();
        for (UserHandle user : profiles) {
            // Query for the set of apps
            String userStr = user.toString();
            Log.i(TAG, "LoadAppAllsActivity-loadAllApps: userStr " + userStr);
            final List<LauncherActivityInfo> apps = mLauncherApps.getActivityList(null, user);//61
            // Fail if we don't have any apps
            // TODO: Fix this. Only fail for the current user.
            if (apps == null || apps.isEmpty()) {
                return;
            }
            // Create the ApplicationInfos
            Log.i(TAG, "LoadAppAllsActivity-loadAllApps:数目 " + apps.size());
            for (int i = 0; i < apps.size(); i++) {
                LauncherActivityInfo app = apps.get(i);
                String name = app.getName();
                Log.i(TAG, "LoadAppAllsActivity-loadAllApps: " + name);
                // This builds the icon bitmaps.
//                mBgAllAppsList.add(new AppInfo(app, user, quietMode), app);
            }
        }
    }

    public void loadAllThirdApps(View view) {
        //获取全部应用：
        PackageManager packageManager = this.getPackageManager();
        List<PackageInfo> packageInfoList = packageManager.getInstalledPackages(0);
        //判断是否系统应用：
        List<PackageInfo> apps = new ArrayList<PackageInfo>();
        Log.i(TAG, "LoadAppAllsActivity-loadAllThirdApps:数目 " + packageInfoList.size());//203
        for (int i = 0; i < packageInfoList.size(); i++) {
            PackageInfo pak = (PackageInfo) packageInfoList.get(i);
            String packageName = pak.packageName;
            //判断是否为系统预装的应用
            if ((pak.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {
                // 第三方应用
                Log.i(TAG, "LoadAppAllsActivity-loadAllThirdApps: 包名：" + packageName + "=三方的");
                apps.add(pak);
            } else {
                //系统应用
                Log.i(TAG, "LoadAppAllsActivity-loadAllThirdApps: 包名：" + packageName + "=系统的");
                if (!packageName.startsWith("com.android.") && !packageName.startsWith("com.huawei.")){
                    apps.add(pak);
                }
            }
        }

        Log.i(TAG, "LoadAppAllsActivity-loadAllThirdApps: 筛选第三方应用");
    }
}
