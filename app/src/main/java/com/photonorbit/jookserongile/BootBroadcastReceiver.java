package com.photonorbit.jookserongile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("setup", "boot.onReceive");
        context.startService(new Intent(context, WidgetBackgroundService.class));
    }
}
