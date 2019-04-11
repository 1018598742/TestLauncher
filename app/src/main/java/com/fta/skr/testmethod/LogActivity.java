package com.fta.skr.testmethod;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

public class LogActivity extends AppCompatActivity {

    //各个Log级别定义的值，级别越高值越大
    /*
        public static final int VERBOSE = 2;
        public static final int DEBUG = 3;
        public static final int INFO = 4;
        public static final int WARN = 5;
        public static final int ERROR = 6;
        public static final int ASSERT = 7;
    */

    private static final String TAG = "Hello";
    private static final String TEST_TAG = "Test_Tag";
    //定义全局的Log开关
    private boolean VERBOSE = Log.isLoggable(TAG, Log.VERBOSE);
    private boolean DEBUG = Log.isLoggable(TAG, Log.DEBUG);
    private boolean INFO = Log.isLoggable(TAG, Log.INFO);
    private boolean WARN = Log.isLoggable(TAG, Log.WARN);
    private boolean ERROR = Log.isLoggable(TAG, Log.ERROR);
    private boolean ASSERT = Log.isLoggable(TAG, Log.ASSERT);
    private boolean SUPPRESS = Log.isLoggable(TAG, -1);


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
    }

    /**
     * 更新loggable值
     */
    private void update() {
        //局部的Log开关
        VERBOSE = Log.isLoggable(TAG, Log.VERBOSE);
        DEBUG = Log.isLoggable(TAG, Log.DEBUG);
        INFO = Log.isLoggable(TAG, Log.INFO);
        WARN = Log.isLoggable(TAG, Log.WARN);
        ERROR = Log.isLoggable(TAG, Log.ERROR);
        ASSERT = Log.isLoggable(TAG, Log.ASSERT);
        SUPPRESS = Log.isLoggable(TAG, -1);
    }

    public void logClick(View view) {
        update();

        Log.d(TEST_TAG, "------------------start------------------------");
        if (VERBOSE) {
            Log.d(TEST_TAG, "verbose log");
        }
        if (DEBUG) {
            Log.d(TEST_TAG, "debug log");
        }
        if (INFO) {
            Log.d(TEST_TAG, "info log");
        }
        if (WARN) {
            Log.d(TEST_TAG, "warn log");
        }
        if (ERROR) {
            Log.d(TEST_TAG, "error log");
        }
        if (ASSERT) {
            Log.d(TEST_TAG, "assert log");
        }
        if (SUPPRESS) {
            Log.d(TEST_TAG, "suppress log");
        }
        Log.d(TEST_TAG, "------------------end------------------------");

    }
}
