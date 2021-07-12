package net.gauso001.shinigami_chanaiclient;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import static android.content.Context.JOB_SCHEDULER_SERVICE;

public class boot_complete_sync_starter extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        ComponentName serviceComponent = new ComponentName(context, Server_sync_service.class);
        JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent);
        builder.setMinimumLatency(10000); // wait at least 10 seconds, consider making 5
        builder.setOverrideDeadline(300000); // maximum delay
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(builder.build());
    }
}
