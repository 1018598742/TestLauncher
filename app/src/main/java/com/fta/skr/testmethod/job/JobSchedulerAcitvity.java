package com.fta.skr.testmethod.job;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.fta.skr.testmethod.R;
import com.fta.skr.testmethod.config.TagConfig;

import java.lang.ref.WeakReference;

public class JobSchedulerAcitvity extends AppCompatActivity {

    private static final String TAG = TagConfig.TAG;

    public static final String MESSENGER_INTENT_KEY = "SchedulerAcitvity.MESSENGER_INTENT_KEY";
    public static final String WORK_DURATION_KEY = "SchedulerAcitvity.WORK_DURATION_KEY";
    public static final int MSG_JOB_START = 0;
    public static final int MSG_JOB_STOP = 1;
    public static final int MSG_ONJOB_START = 2;
    public static final int MSG_ONJOB_STOP = 3;

    private int mJobId = 0;// 执行的JobId

    ComponentName mServiceComponent;// 这就是我们的jobservice组件了
//    private IncomingMessageHandler mHandler;// 用于来自服务的传入消息的处理程序。

    // UI
    private EditText mEt_Delay;// 设置delay时间
    private EditText mEt_Deadline;// 设置最长的截止时间
    private EditText mEt_DurationTime;// setPeriodic周期
    private RadioButton mRb_WiFiConnectivity;// 设置builder中的是否有WiFi连接
    private RadioButton mRb_AnyConnectivity;// 设置builder中的是否有网络即可
    private CheckBox mCb_RequiresCharging;// 设置builder中的是否需要充电
    private CheckBox mCb_RequiresIdle;// 设置builder中的是否设备空闲
    private Button mBtn_StartJob;// 点击开始任务的按钮
    private Button mBtn_StopAllJob;// 点击结束所有任务的按钮


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_scheduler);

//        mHandler = new IncomingMessageHandler(this);
        mServiceComponent = new ComponentName(this, MyJobService.class);// 获取到我们自己的jobservice，同时启动该service

        // 设置UI
        mEt_Delay = (EditText) findViewById(R.id.delay_time);
        mEt_DurationTime = (EditText) findViewById(R.id.duration_time);
        mEt_Deadline = (EditText) findViewById(R.id.deadline_time);
        mRb_WiFiConnectivity = (RadioButton) findViewById(R.id.checkbox_unmetered);
        mRb_AnyConnectivity = (RadioButton) findViewById(R.id.checkbox_any);
        mCb_RequiresCharging = (CheckBox) findViewById(R.id.checkbox_charging);
        mCb_RequiresIdle = (CheckBox) findViewById(R.id.checkbox_idle);
        mBtn_StartJob = (Button) findViewById(R.id.button_start_job);
        mBtn_StopAllJob = (Button) findViewById(R.id.button_stop_job);

        mBtn_StartJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    scheduleJob();
                }
            }
        });

        mBtn_StopAllJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    cancelAllJobs();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 启动服务并提供一种与此类通信的方法。
//        Intent startServiceIntent = new Intent(this, MyJobService.class);
//        Messenger messengerIncoming = new Messenger(mHandler);
//        startServiceIntent.putExtra(MESSENGER_INTENT_KEY, messengerIncoming);
//        startService(startServiceIntent);
    }

    @Override
    protected void onStop() {
        // 服务可以是“开始”和/或“绑定”。 在这种情况下，它由此Activity“启动”
        // 和“绑定”到JobScheduler（也被JobScheduler称为“Scheduled”）。
        // 对stopService（）的调用不会阻止处理预定作业。
        // 然而，调用stopService（）失败将使它一直存活。
        stopService(new Intent(this, MyJobService.class));
        super.onStop();
    }

    // 当用户单击SCHEDULE JOB时执行。
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void scheduleJob() {
        //开始配置JobInfo
        JobInfo.Builder builder = new JobInfo.Builder(mJobId++, mServiceComponent);

        //设置任务的延迟执行时间(单位是毫秒)
//        String delay = mEt_Delay.getText().toString();
//        if (!TextUtils.isEmpty(delay)) {
//            builder.setMinimumLatency(Long.valueOf(delay) * 1000);
//        }
        //设置任务最晚的延迟时间。如果到了规定的时间时其他条件还未满足，你的任务也会被启动。
//        String deadline = mEt_Deadline.getText().toString();
//        if (!TextUtils.isEmpty(deadline)) {
//            builder.setOverrideDeadline(Long.valueOf(deadline) * 1000);
//        }
        boolean requiresUnmetered = mRb_WiFiConnectivity.isChecked();
        boolean requiresAnyConnectivity = mRb_AnyConnectivity.isChecked();

        //让你这个任务只有在满足指定的网络条件时才会被执行
        if (requiresUnmetered) {
            //wifi
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);
        } else if (requiresAnyConnectivity) {
            //有网就行
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        }

        //你的任务只有当用户没有在使用该设备且有一段时间没有使用时才会启动该任务。
        builder.setRequiresDeviceIdle(mCb_RequiresIdle.isChecked());
        //告诉你的应用，只有当设备在充电时这个任务才会被执行。
        builder.setRequiresCharging(mCb_RequiresCharging.isChecked());

        // Extras, work duration.
//        PersistableBundle extras = new PersistableBundle();
//        String workDuration = mEt_DurationTime.getText().toString();
//        if (TextUtils.isEmpty(workDuration)) {
//            workDuration = "1";
//        }
//        extras.putLong(WORK_DURATION_KEY, Long.valueOf(workDuration) * 1000);
//
//
//        builder.setExtras(extras);

        String workDuration = mEt_DurationTime.getText().toString();
        if (!workDuration.isEmpty()) {
            builder.setPeriodic(Long.valueOf(workDuration));
        }

        // Schedule job
        Log.d(TAG, "JobSchedulerAcitvity-scheduleJob: Scheduling job");
        JobScheduler mJobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        // 这里就将开始在service里边处理我们配置好的job
        int schedule = mJobScheduler.schedule(builder.build());
        Log.d(TAG, "JobSchedulerAcitvity-scheduleJob: 返回码：" + schedule);

        //mJobScheduler.schedule(builder.build())会返回一个int类型的数据
        //如果schedule方法失败了，它会返回一个小于0的错误码。否则它会返回我们在JobInfo.Builder中定义的标识id。
    }

    // 当用户点击取消所有时执行
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void cancelAllJobs() {
        JobScheduler mJobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        mJobScheduler.cancelAll();
        Toast.makeText(JobSchedulerAcitvity.this, R.string.all_jobs_cancelled, Toast.LENGTH_SHORT).show();
    }

    /**
     * {@link Handler}允许您发送与线程相关联的消息。
     * {@link Messenger}使用此处理程序从{@link MyJobService}进行通信。
     * 它也用于使开始和停止视图在短时间内闪烁。
     */
//    private static class IncomingMessageHandler extends Handler {
//
//        // 使用弱引用防止内存泄露
//        private WeakReference<JobSchedulerAcitvity> mActivity;
//
//        IncomingMessageHandler(JobSchedulerAcitvity activity) {
//            super(/* default looper */);
//            this.mActivity = new WeakReference<>(activity);
//        }
//
//        @Override
//        public void handleMessage(Message msg) {
//            JobSchedulerAcitvity mSchedulerAcitvity = mActivity.get();
//            if (mSchedulerAcitvity == null) {
//                // 活动不再可用，退出。
//                return;
//            }
//
//            // 获取到两个View，用于之后根据Job运行状态显示不同的运行状态（颜色变化）
//            View showStartView = mSchedulerAcitvity.findViewById(R.id.onstart_textview);
//            View showStopView = mSchedulerAcitvity.findViewById(R.id.onstop_textview);
//
//            Message m;
//            switch (msg.what) {
//                // 当作业登录到应用程序时，从服务接收回调。 打开指示灯（上方View闪烁）并发送一条消息，在一秒钟后将其关闭。
//                case MSG_JOB_START:
//                    // Start received, turn on the indicator and show text.
//                    // 开始接收，打开指示灯（上方View闪烁）并显示文字。
//                    showStartView.setBackgroundColor(getColor(R.color.start_received));
//                    updateParamsTextView(msg.obj, "started");
//
//                    // Send message to turn it off after a second.
//                    // 发送消息，一秒钟后关闭它。
//                    m = Message.obtain(this, MSG_ONJOB_START);
//                    sendMessageDelayed(m, 1000L);
//                    break;
//
//                // 当先前执行在应用程序中的作业必须停止执行时，
//                // 从服务接收回调。 打开指示灯并发送一条消息，
//                // 在两秒钟后将其关闭。
//                case MSG_JOB_STOP:
//                    // Stop received, turn on the indicator and show text.
//                    // 停止接收，打开指示灯并显示文本。
//                    showStopView.setBackgroundColor(getColor(R.color.stop_received));
//                    updateParamsTextView(msg.obj, "stopped");
//
//                    // Send message to turn it off after a second.
//                    // 发送消息，一秒钟后关闭它。
//                    m = obtainMessage(MSG_ONJOB_STOP);
//                    sendMessageDelayed(m, 2000L);
//                    break;
//                case MSG_ONJOB_START:
//                    showStartView.setBackgroundColor(getColor(R.color.none_received));
//                    updateParamsTextView(null, "job had started");
//                    break;
//                case MSG_ONJOB_STOP:
//                    showStopView.setBackgroundColor(getColor(R.color.none_received));
//                    updateParamsTextView(null, "job had stoped");
//                    break;
//            }
//        }
//
//        // 更新UI显示
//        // @param jobId jobId
//        // @param action 消息
//        private void updateParamsTextView(@Nullable Object jobId, String action) {
//            TextView paramsTextView = (TextView) mActivity.get().findViewById(R.id.task_params);
//            if (jobId == null) {
//                paramsTextView.setText("");
//                return;
//            }
//            String jobIdText = String.valueOf(jobId);
//            paramsTextView.setText(String.format("Job ID %s %s", jobIdText, action));
//        }
//
//        private int getColor(@ColorRes int color) {
//            return mActivity.get().getResources().getColor(color);
//        }
//    }
}
