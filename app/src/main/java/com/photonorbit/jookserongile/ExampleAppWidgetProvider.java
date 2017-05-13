package com.photonorbit.jookserongile;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Arrays;

public class ExampleAppWidgetProvider extends AppWidgetProvider {

    private BroadcastReceiver receiver;
    static int[] allAppWidgetIds;
    static RemoteViews[] remoteViews;

    protected void updateContent(AppWidgetManager widgetManager, RemoteViews view, int appWidgetId) {
        long t = System.currentTimeMillis();
        view.setTextViewText(R.id.tekst, appWidgetId + ": " + t);
        widgetManager.updateAppWidget(appWidgetId, view);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.i("setup", "onReceive mina: " + ExampleAppWidgetProvider.this + ", action == " + intent.getAction());
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.i("setup", "onUpdate mina: " + ExampleAppWidgetProvider.this);
        Log.i("setup", "onUpdate (1.0) remoteViews.length == " + (remoteViews == null ? "null" : remoteViews.length));
        Log.i("setup", "onUpdate (1.1) allAppWidgetIds.length == " + (this.allAppWidgetIds == null ? "null" : this.allAppWidgetIds.length));
        Log.i("setup", "onUpdate (1.2) allAppWidgetIds == " + Arrays.toString(this.allAppWidgetIds));
        // Liita appWidgetIds olemasolevatele, teha uus remoteViews ja uuendada kõiki.
        // Mõistlik on uuendada enne kui minuti tick kohale jõuab, muidu näeb kasutaja tühja pilti.
        this.allAppWidgetIds = mergeArrays(this.allAppWidgetIds, appWidgetIds);
        Log.i("setup", "onUpdate (1.3) allAppWidgetIds == " + Arrays.toString(this.allAppWidgetIds));
        remoteViews = new RemoteViews[this.allAppWidgetIds.length];
        for (int i = 0; i < this.allAppWidgetIds.length ; i++) {
            int widgetId = this.allAppWidgetIds[i];
            remoteViews[i] = new RemoteViews(context.getPackageName(), R.layout.example_appwidget);
            updateContent(appWidgetManager, remoteViews[i], widgetId);

/*
            Intent intent = new Intent(context, ExampleAppWidgetProvider.class);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allAppWidgetIds);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
*/
        }
        Log.i("setup", "onUpdate (2) remoteViews.length == " + (remoteViews == null ? "null" : remoteViews.length));
    }

    @Override
    public void onEnabled(final Context context) {
        super.onEnabled(context);
        Log.i("setup", "onEnabled mina: " + ExampleAppWidgetProvider.this);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context ctx, Intent intent)
            {
                if (intent.getAction().compareTo(Intent.ACTION_TIME_TICK) == 0) {
                    Log.i("minute", "Minut möödunud! mina: " + ExampleAppWidgetProvider.this);
                    AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
                    if (remoteViews == null || allAppWidgetIds == null) {
                        Log.w("minute", "remoteViews == null");
                        allAppWidgetIds = widgetManager.getAppWidgetIds(new ComponentName(context, ExampleAppWidgetProvider.class));
                        Log.w("minute", "allAppWidgetIds.length == " + allAppWidgetIds.length);
                        remoteViews = new RemoteViews[allAppWidgetIds.length];
                        for (int i = 0; i < allAppWidgetIds.length ; i++) {
                            int widgetId = allAppWidgetIds[i];
                            remoteViews[i] = new RemoteViews(context.getPackageName(), R.layout.example_appwidget);
                        }
                    }
                    Log.w("minute", "remoteViews.length == " + remoteViews.length);
                    for (int i = 0 ; remoteViews != null && i < remoteViews.length ; i++) {
                        updateContent(widgetManager, remoteViews[i], allAppWidgetIds[i]);
                    }
                }
            }
        };

        context.getApplicationContext().registerReceiver(receiver, new IntentFilter(Intent.ACTION_TIME_TICK));
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        Log.i("setup", "onDeleted mina: " + ExampleAppWidgetProvider.this);
        Log.i("setup", "onDeleted appWidgetIds = " + Arrays.toString(appWidgetIds));
        this.allAppWidgetIds = null;
        this.remoteViews = null;
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        Log.i("setup", "onDisabled mina: " + ExampleAppWidgetProvider.this);
        if (receiver != null)
            context.getApplicationContext().unregisterReceiver(receiver);
    }

    public static int[] mergeArrays(int[] a, int[] b) {
        if (a == null && b == null) {
            return null;
        }
        if (a == null) {
            a = new int[0];
        } else if (b == null) {
            b = new int[0];
        }

        int[] c = new int[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        int ic = a.length;
        loopb: for (int ib = 0 ; ib < b.length ; ib++) {
            for (int ia = 0 ; ia < a.length ; ia++) {
                if (a[ia] == b[ib]) {
                    continue loopb;
                }
            }
            c[ic] = b[ib];
            ic++;
        }

        if (ic < c.length) {
            int[] d = new int[ic];
            System.arraycopy(c, 0, d, 0, ic);
            c = d;
        }
        return c;
    }
}
