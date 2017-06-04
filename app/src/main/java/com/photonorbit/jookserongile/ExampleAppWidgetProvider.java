package com.photonorbit.jookserongile;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Arrays;
import java.util.List;

public class ExampleAppWidgetProvider extends AppWidgetProvider {

    private static final String APP_WIDGET_ID_KEY = "appWidgetIdKey";

    private BroadcastReceiver receiver;
    static int[] allAppWidgetIds;
    static RemoteViews[] remoteViews;

    public static class BootBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("setup", "boot.onReceive");
            context.startService(new Intent(context, ClockUpdateService.class));
        }
    }

    public static class ClockUpdateService extends Service {
        private static final String ACTION_UPDATE = "com.photonorbit.jookserongile.action.UPDATE";

        private final static IntentFilter intentFilter;

        static {
            intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_TIME_TICK);
            intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
            intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
            intentFilter.addAction(ACTION_UPDATE);
        }

        private final BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateTables(context, intent);
            }
        };

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public void onCreate() {
            super.onCreate();

            registerReceiver(receiver, intentFilter);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();

            unregisterReceiver(receiver);
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            if (intent != null && intent.getAction() != null) {
                if (intent.getAction().equals(ACTION_UPDATE)) {
                    updateTables(this, intent);
                }
            }
            return START_STICKY;
        }
    }

    private static void updateTables(Context context, Intent intent) {
        Log.i("data", "updateTables");
        if (intent.getAction().compareTo(Intent.ACTION_TIME_TICK) == 0) {
            Log.i("minute", "Minut möödunud!");
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
                updateContent(context, widgetManager, remoteViews[i], allAppWidgetIds[i]);
            }
        }
    }

    protected static void updateContent(final Context context, final AppWidgetManager widgetManager, final RemoteViews view, final int appWidgetId) {
        long t = System.currentTimeMillis();
        List<DataUtil.Row> data = DataUtil.getNextRows(context, Integer.toString(appWidgetId), t, 4, new DataUtil.DoneCallback() {
            @Override
            public void done() {
                Log.i("data", "Done, calling for an update.");
                Intent updateIntent = new Intent();
                updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                updateIntent.putExtra(APP_WIDGET_ID_KEY, appWidgetId);
                context.sendBroadcast(updateIntent);
            }
        });
        for (DataUtil.Row row : data) {
            Log.i("data", row.toString());
        }
        String text = appWidgetId + ": [" + data.size() + "]";
        if (data.size() > 0) {
            text += " " + data.get(0).line + " " + data.get(0).time;
        }
        view.setTextViewText(R.id.tekst, text);
        widgetManager.updateAppWidget(appWidgetId, view);
    }

    protected void inflate(Context context, int appWidgetId) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.example_appwidget);
//        remoteViews.
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("setup", "onReceive mina: " + ExampleAppWidgetProvider.this + ", action == " + intent.getAction());
        super.onReceive(context, intent);
        if (intent.hasExtra(APP_WIDGET_ID_KEY)) {
            onUpdate(context, AppWidgetManager.getInstance(context), new int[]{intent.getIntExtra(APP_WIDGET_ID_KEY, -1)});
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.i("setup", "onUpdate mina: " + ExampleAppWidgetProvider.this);
        context.startService(new Intent(context, ClockUpdateService.class));
        for (int appWidgetId : appWidgetIds) {
            inflate(context, appWidgetId);
        }
        Log.i("setup", "onUpdate (1.0) remoteViews.length == " + (remoteViews == null ? "null" : remoteViews.length));
        Log.i("setup", "onUpdate (1.1) allAppWidgetIds.length == " + (this.allAppWidgetIds == null ? "null" : this.allAppWidgetIds.length));
        Log.i("setup", "onUpdate (1.2) allAppWidgetIds == " + Arrays.toString(this.allAppWidgetIds));
        Log.i("setup", "onUpdate (1.2) appWidgetIds == " + Arrays.toString(appWidgetIds));
        // Liita appWidgetIds olemasolevatele, teha uus remoteViews ja uuendada kõiki.
        // Mõistlik on uuendada enne kui minuti tick kohale jõuab, muidu näeb kasutaja tühja pilti.
        this.allAppWidgetIds = mergeArrays(this.allAppWidgetIds, appWidgetIds);
        Log.i("setup", "onUpdate (1.3) allAppWidgetIds == " + Arrays.toString(this.allAppWidgetIds));
        remoteViews = new RemoteViews[this.allAppWidgetIds.length];
        for (int i = 0; i < this.allAppWidgetIds.length ; i++) {
            int widgetId = this.allAppWidgetIds[i];
            remoteViews[i] = new RemoteViews(context.getPackageName(), R.layout.example_appwidget);
            updateContent(context, appWidgetManager, remoteViews[i], widgetId);

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
        Log.i("setup", "onEnabled mina: " + ExampleAppWidgetProvider.this);
        super.onEnabled(context);
        context.startService(new Intent(context, ClockUpdateService.class));
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
        context.stopService(new Intent(context, ClockUpdateService.class));
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
