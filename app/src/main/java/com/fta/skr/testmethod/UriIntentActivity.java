package com.fta.skr.testmethod;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.net.URISyntaxException;
import java.util.List;

public class UriIntentActivity extends AppCompatActivity {

    private static final String TAG = "My_Test";
    private Context mContext;

    private PackageManager mPackageManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uri_intent);

        mContext = this;
        mPackageManager = mContext.getPackageManager();
    }

    public void findActiv(View view) {

        try {
            Intent intent1 = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"));


            String uri = intent1.toUri(0);
            Log.i(TAG, "UriIntentActivity-findActiv: uri is " + uri);
            Intent metaIntent = Intent.parseUri(uri, 0);
            ResolveInfo resolved = mPackageManager.resolveActivity(metaIntent, PackageManager.MATCH_DEFAULT_ONLY);
            List<ResolveInfo> appList = mPackageManager.queryIntentActivities(metaIntent, PackageManager.MATCH_DEFAULT_ONLY);
            if (wouldLaunchResolverActivity(resolved, appList)) {
                // If only one of the results is a system app then choose that as the default.
                final ResolveInfo systemApp = getSingleSystemActivity(appList);
                if (systemApp == null) {
                    // There is no logical choice for this meta-favorite, so rather than making
                    // a bad choice just add nothing.
                    Log.w(TAG, "No preference or single system activity found for "
                            + metaIntent.toString());
                    return;
                }
                resolved = systemApp;
            }

            final ActivityInfo info = resolved.activityInfo;
            final Intent intent = mPackageManager.getLaunchIntentForPackage(info.packageName);
            if (intent == null) {
                Log.i(TAG, "UriIntentActivity-findActiv: intent is null");
                return;
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            String intentStr = intent.toUri(0);
            String appName = info.loadLabel(mPackageManager).toString();
            Log.i(TAG, "UriIntentActivity-findActiv: appName is " + appName + " intent is " + intentStr);

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

    }

    private ResolveInfo getSingleSystemActivity(List<ResolveInfo> appList) {
        ResolveInfo systemResolve = null;
        final int N = appList.size();
        for (int i = 0; i < N; ++i) {
            try {
                ApplicationInfo info = mPackageManager.getApplicationInfo(
                        appList.get(i).activityInfo.packageName, 0);
                if ((info.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                    if (systemResolve != null) {
                        return null;
                    } else {
                        systemResolve = appList.get(i);
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.w(TAG, "Unable to get info about resolve results", e);
                return null;
            }
        }
        return systemResolve;
    }

    private boolean wouldLaunchResolverActivity(ResolveInfo resolved,
                                                List<ResolveInfo> appList) {
        // If the list contains the above resolved activity, then it can't be
        // ResolverActivity itself.
        for (int i = 0; i < appList.size(); ++i) {
            ResolveInfo tmp = appList.get(i);
            if (tmp.activityInfo.name.equals(resolved.activityInfo.name)
                    && tmp.activityInfo.packageName.equals(resolved.activityInfo.packageName)) {
                return false;
            }
        }
        return true;
    }

    public void jumpSys(View view) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
