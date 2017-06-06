package com.photonorbit.jookserongile;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class WidgetBackgroundService extends Service {
    private static final String ACTION_UPDATE = "com.photonorbit.jookserongile.action.UPDATE";

    private static final String TAG = "WidgetBackground";
    private static BroadcastReceiver mMinuteTickReceiver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy(){
        if(mMinuteTickReceiver!=null) {
            unregisterReceiver(mMinuteTickReceiver);
            mMinuteTickReceiver = null;
        }

        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null) {
            if (intent.hasExtra("SHUTDOWN")) {
                if (intent.getBooleanExtra("SHUTDOWN", false)) {

                    if(mMinuteTickReceiver!=null) {
                        unregisterReceiver(mMinuteTickReceiver);
                        mMinuteTickReceiver = null;
                    }
                    stopSelf();
                    return START_NOT_STICKY;
                }
            }
        }

        if(mMinuteTickReceiver==null) {
            registerOnTickReceiver();
        }
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    private void registerOnTickReceiver() {
        mMinuteTickReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent){
                Intent timeTick=new Intent(ExampleAppWidgetProvider.ACTION_TICK);
                sendBroadcast(timeTick);
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(mMinuteTickReceiver, filter);
    }
}
