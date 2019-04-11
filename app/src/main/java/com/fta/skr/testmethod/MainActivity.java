package com.fta.skr.testmethod;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Xml;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "my_test";

    private Context mContext;
    private EditText mEt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        testMethod(this);

        mEt = ((EditText) findViewById(R.id.packageEt));

    }

    float minWidthDps;
    float minHeightDps;

    private void testMethod(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        display.getMetrics(dm);

        Point smallestSize = new Point();
        Point largestSize = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            display.getCurrentSizeRange(smallestSize, largestSize);
        }

        Log.i(TAG, "MainActivity-testMethod: smallestSize=" + smallestSize + "=largestSize=" + largestSize);
        // This guarantees that width < height
        minWidthDps = Utilities.dpiFromPx(Math.min(smallestSize.x, smallestSize.y), dm);
        minHeightDps = Utilities.dpiFromPx(Math.min(largestSize.x, largestSize.y), dm);

        Log.i(TAG, "MainActivity-testMethod: minWidthDps=" + minWidthDps + "=minHeightDps=" + minHeightDps);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void handleXml(View view) {

        try (XmlResourceParser parser = mContext.getResources().getXml(R.xml.device_profiles)) {
            final int depth = parser.getDepth();
            int type;


            while (((type = parser.next()) != XmlPullParser.END_TAG || parser.getDepth() > depth) && type != XmlPullParser.END_DOCUMENT) {
                String name = parser.getName();
                Log.i(TAG, "MainActivity-handleXml: name=" + name + "=type=" + type);
                if ((type == XmlPullParser.START_TAG) && "profile".equals(name)) {
                    TypedArray a = mContext.obtainStyledAttributes(
                            Xml.asAttributeSet(parser), R.styleable.InvariantDeviceProfile);
                    int numRows = a.getInt(R.styleable.InvariantDeviceProfile_numRows, 0);
                    int numColumns = a.getInt(R.styleable.InvariantDeviceProfile_numColumns, 0);
                    float iconSize = a.getFloat(R.styleable.InvariantDeviceProfile_iconSize, 0);
                    InvariantDeviceProfile invariantDeviceProfile = new InvariantDeviceProfile(
                            a.getString(R.styleable.InvariantDeviceProfile_name),
                            a.getFloat(R.styleable.InvariantDeviceProfile_minWidthDps, 0),
                            a.getFloat(R.styleable.InvariantDeviceProfile_minHeightDps, 0),
                            numRows,
                            numColumns,
                            a.getInt(R.styleable.InvariantDeviceProfile_numFolderRows, numRows),
                            a.getInt(R.styleable.InvariantDeviceProfile_numFolderColumns, numColumns),
                            iconSize,
                            a.getFloat(R.styleable.InvariantDeviceProfile_landscapeIconSize, iconSize),
                            a.getFloat(R.styleable.InvariantDeviceProfile_iconTextSize, 0),
                            a.getInt(R.styleable.InvariantDeviceProfile_numHotseatIcons, numColumns),
                            a.getResourceId(R.styleable.InvariantDeviceProfile_defaultLayoutId, 0),
                            a.getResourceId(R.styleable.InvariantDeviceProfile_demoModeLayoutId, 0));
                    Log.i(TAG, "MainActivity-handleXml: " + invariantDeviceProfile.toString());
                    a.recycle();
                }
            }

        } catch (IOException | XmlPullParserException e) {
            throw new RuntimeException();
        }
    }

    public void secondActivity(View view) {
        Intent intent = new Intent(this, SecondActivity.class);
        startActivity(intent);
    }

    /**
     * 根据用户获取所有应用
     *
     * @param view
     */
    public void obtainAllApps(View view) throws PackageManager.NameNotFoundException {
        //>=17 >= 21 >=24
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            UserManager userManager = (UserManager) mContext.getSystemService(Context.USER_SERVICE);
            List<UserHandle> userProfiles = userManager.getUserProfiles();
            LauncherApps launcherApps = (LauncherApps) mContext.getSystemService(Context.LAUNCHER_APPS_SERVICE);
            PackageManager packageManager = mContext.getPackageManager();
            for (UserHandle user : userProfiles) {
                List<LauncherActivityInfo> activityList = launcherApps.getActivityList(null, user);
                if (activityList != null && !activityList.isEmpty()) {
                    Log.i(TAG, "MainActivity-obtainAllApps: 数目：" + activityList.size());
                    for (LauncherActivityInfo launcherActivityInfo : activityList) {
                        ComponentName componentName = launcherActivityInfo.getComponentName();
                        String name = launcherActivityInfo.getName();
                        String packageName = launcherActivityInfo.getApplicationInfo().packageName;

                        boolean equals = Process.myUserHandle().equals(user);
                        int flags = equals ? 0 : PackageManager.GET_UNINSTALLED_PACKAGES;
                        PackageInfo packageInfo = packageManager.getPackageInfo(packageName, flags);
                        ApplicationInfo applicationInfo = packageInfo.applicationInfo;
                        CharSequence appNames = applicationInfo.loadLabel(packageManager);

                        boolean equalUser = launcherActivityInfo.getUser().equals(user);
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("名称：").append(appNames).append("\t启动activity名称：").append(name).append("\t包名：").append(packageName).append("\t是否这个用户：").append(equalUser);
                        Log.i(TAG, "MainActivity-obtainAllApps: " + stringBuilder.toString());
                    }
                }
            }

            PackageInstaller packageInstaller = mContext.getPackageManager().getPackageInstaller();
            List<PackageInstaller.SessionInfo> allSessions = packageInstaller.getAllSessions();


        }

    }

    public void handleXmlActivity(View view) {
        startActivity(new Intent(this, HandleXmlActivity.class));
    }

    public void obtainIntent(View view) {
        startActivity(new Intent(this, UriIntentActivity.class));
    }

    public void findSysApp(View view) {
        //获取全部应用：
        PackageManager packageManager = this.getPackageManager();
        List<PackageInfo> packageInfoList = packageManager.getInstalledPackages(0);
        //判断是否系统应用：
        List<PackageInfo> apps = new ArrayList<PackageInfo>();
        for (int i = 0; i < packageInfoList.size(); i++) {
            PackageInfo pak = (PackageInfo) packageInfoList.get(i);
            String packageName = pak.packageName;
            //判断是否为系统预装的应用
            if ((pak.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {
                // 第三方应用
                Log.i(TAG, "MainActivity-findSysApp: 包名：" + packageName + "=三方的");
            } else {
                //系统应用
                Log.i(TAG, "MainActivity-findSysApp: 包名：" + packageName + "=系统的");
            }

            if (isUserApp(pak)) {
                Log.i(TAG, "MainActivity-findSysApp: 用户应用：" + packageName);
            }
        }

    }


    private boolean isSystemApp(PackageInfo pInfo) {
        return ((pInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
    }

    private boolean isSystemUpdateApp(PackageInfo pInfo) {
        return ((pInfo.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0);
    }

    private boolean isUserApp(PackageInfo pInfo) {
        return (!isSystemApp(pInfo) && !isSystemUpdateApp(pInfo));
    }


    public void uninstallApp(View view) {
        String packageName = mEt.getText().toString().trim();
        if (TextUtils.isEmpty(packageName)) {
            packageName = "com.taobao.taobao";
        }

        Uri packageURI = Uri.parse("package:" + packageName);
        Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
        uninstallIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(uninstallIntent);
    }

    public void toJumpLog(View view) {
        startActivity(new Intent(this,LogActivity.class));
    }
}