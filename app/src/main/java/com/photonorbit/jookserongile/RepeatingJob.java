package com.photonorbit.jookserongile;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class RepeatingJob extends JobService {
    private final static String TAG = "RepeatingJob";

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "onStartJob");
        Intent intent=new Intent(ExampleAppWidgetProvider.JOB_TICK);
        sendBroadcast(intent);
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

}
