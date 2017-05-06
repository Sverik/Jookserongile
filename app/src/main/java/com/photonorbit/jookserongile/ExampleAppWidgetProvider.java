package com.photonorbit.jookserongile;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.RemoteViews;

public class ExampleAppWidgetProvider extends AppWidgetProvider {

    private BroadcastReceiver receiver;
    int[] appWidgetIds;
    RemoteViews[] remoteViews;

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        this.appWidgetIds = appWidgetIds;
        remoteViews = new RemoteViews[appWidgetIds.length];
        for (int i = 0 ; i < appWidgetIds.length ; i++) {
            int widgetId = appWidgetIds[i];
            remoteViews[i] = new RemoteViews(context.getPackageName(), R.layout.example_appwidget);
            remoteViews[i].setTextViewText(R.id.tekst, "M " + widgetId);

/*
            Intent intent = new Intent(context, ExampleAppWidgetProvider.class);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
*/
            appWidgetManager.updateAppWidget(widgetId, remoteViews[i]);
        }
    }

    @Override
    public void onEnabled(final Context context) {
        super.onEnabled(context);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context ctx, Intent intent)
            {
                if (intent.getAction().compareTo(Intent.ACTION_TIME_TICK) == 0) {
                    Log.i("minute", "Minut möödunud!");
                    AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
                    long t = System.currentTimeMillis();
                    if (remoteViews == null) {
                        Log.w("minute", "remoteViews == null");
                        appWidgetIds = widgetManager.getAppWidgetIds(new ComponentName(context, ExampleAppWidgetProvider.class));
                        Log.w("minute", "appWidgetIds.length == " + appWidgetIds.length);
                        remoteViews = new RemoteViews[appWidgetIds.length];
                        for (int i = 0 ; i < appWidgetIds.length ; i++) {
                            int widgetId = appWidgetIds[i];
                            remoteViews[i] = new RemoteViews(context.getPackageName(), R.layout.example_appwidget);
                        }
                    }
                    Log.w("minute", "remoteViews.length == " + remoteViews.length);
                    for (int i = 0 ; remoteViews != null && i < remoteViews.length ; i++) {
                        remoteViews[i].setTextViewText(R.id.tekst, appWidgetIds[i] + ": " + t);
                        widgetManager.updateAppWidget(appWidgetIds[i], remoteViews[i]);
                    }
                }
            }
        };

        context.getApplicationContext().registerReceiver(receiver, new IntentFilter(Intent.ACTION_TIME_TICK));
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        if (receiver != null)
            context.getApplicationContext().unregisterReceiver(receiver);
    }
}
