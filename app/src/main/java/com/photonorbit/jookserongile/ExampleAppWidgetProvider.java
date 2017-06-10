package com.photonorbit.jookserongile;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Arrays;
import java.util.List;

public class ExampleAppWidgetProvider extends AppWidgetProvider {

    private static final String APP_WIDGET_ID_KEY = "appWidgetIdKey";
    public static final String ACTION_TICK = "CLOCK_TICK";
    public static final String SETTINGS_CHANGED = "SETTINGS_CHANGED";
    public static final String JOB_TICK = "JOB_CLOCK_TICK";

    private BroadcastReceiver receiver;
    static int[] allAppWidgetIds;
    static RemoteViews remoteViews = null;
    static RemoteViews[] rows = null;

    public static void updateTables(Context context, Intent intent) {
        Log.i("data", "updateTables");
        if (intent.getAction().compareTo(Intent.ACTION_TIME_TICK) == 0) {
            Log.i("minute", "Minut möödunud!");
            AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
            if (remoteViews == null || allAppWidgetIds == null) {
                Log.w("minute", "remoteViews == null");
                allAppWidgetIds = widgetManager.getAppWidgetIds(new ComponentName(context, ExampleAppWidgetProvider.class));
                Log.w("minute", "allAppWidgetIds.length == " + allAppWidgetIds.length);
                inflate(context);
            }
            Log.w("minute", "remoteViews.length == " + remoteViews);
        }
    }

    protected static void updateContent(final Context context, final AppWidgetManager widgetManager, final int appWidgetId) {
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
        int i = 0;
        for (DataUtil.Row row : data) {
            if (i >= rows.length) {
                break;
            }
            rows[i].setTextViewText(R.id.line, row.time);
            rows[i].setTextViewText(R.id.delta, Integer.toString(i));
            Log.i("data", row.toString());
            i++;
        }
        String text = appWidgetId + " @" + FetchTimesTask.timeToString(System.currentTimeMillis()) + " [" + data.size() + "]";
        if (data.size() > 0) {
            text += " " + data.get(0).line + " " + data.get(0).time;
        }
        remoteViews.setTextViewText(R.id.title, text);
        widgetManager.updateAppWidget(appWidgetId, remoteViews);
    }

    protected static void inflate(Context context) {
        remoteViews = new RemoteViews(context.getPackageName(), R.layout.example_appwidget);
        remoteViews.removeAllViews(R.id.schedule);
        remoteViews.addView(R.id.schedule, new RemoteViews(context.getPackageName(), R.layout.schedule_title));
        rows = new RemoteViews[3];
        for (int i = 0 ; i < rows.length ; i++) {
            rows[i] = new RemoteViews(context.getPackageName(), R.layout.schedule_row);
            remoteViews.addView(R.id.schedule, rows[i]);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("setup", "onReceive mina: " + ExampleAppWidgetProvider.this + ", action == " + intent.getAction());
        super.onReceive(context, intent);
        SharedPreferences preferences =  PreferenceManager.getDefaultSharedPreferences(context);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName thisAppWidget = new ComponentName(context.getPackageName(), ExampleAppWidgetProvider.class.getName());
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);

        if (intent.getAction().equals(SETTINGS_CHANGED)) {
            onUpdate(context, appWidgetManager, appWidgetIds);
            if (appWidgetIds.length > 0) {
                restartAll(context);
            }
        }

        if (intent.getAction().equals(JOB_TICK) || intent.getAction().equals(ACTION_TICK) ||
                intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                || intent.getAction().equals(Intent.ACTION_DATE_CHANGED)
                || intent.getAction().equals(Intent.ACTION_TIME_CHANGED)
                || intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED)) {
            restartAll(context);
            onUpdate(context, appWidgetManager, appWidgetIds);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.i("setup", "onUpdate mina: " + ExampleAppWidgetProvider.this);
        context.startService(new Intent(context, WidgetBackgroundService.class));
        Log.i("setup", "onUpdate (1.0) remoteViews == " + remoteViews);
        Log.i("setup", "onUpdate (1.1) allAppWidgetIds.length == " + (this.allAppWidgetIds == null ? "null" : this.allAppWidgetIds.length));
        Log.i("setup", "onUpdate (1.2) allAppWidgetIds == " + Arrays.toString(this.allAppWidgetIds));
        Log.i("setup", "onUpdate (1.2) appWidgetIds == " + Arrays.toString(appWidgetIds));
        // Liita appWidgetIds olemasolevatele, teha uus remoteViews ja uuendada kõiki.
        // Mõistlik on uuendada enne kui minuti tick kohale jõuab, muidu näeb kasutaja tühja pilti.
        this.allAppWidgetIds = mergeArrays(this.allAppWidgetIds, appWidgetIds);
        Log.i("setup", "onUpdate (1.3) allAppWidgetIds == " + Arrays.toString(this.allAppWidgetIds));
        if (remoteViews == null) {
            inflate(context);
        }
        for (int i = 0; i < this.allAppWidgetIds.length ; i++) {
            int widgetId = this.allAppWidgetIds[i];
            updateContent(context, appWidgetManager, widgetId);

/*
            Intent intent = new Intent(context, ExampleAppWidgetProvider.class);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allAppWidgetIds);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
*/
        }
        Log.i("setup", "onUpdate (2) remoteViews == " + remoteViews);
    }

    @Override
    public void onEnabled(final Context context) {
        Log.i("setup", "onEnabled mina: " + ExampleAppWidgetProvider.this);
        super.onEnabled(context);
        restartAll(context);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            jobScheduler.cancelAll();
        } else {
            // stop alarm
            AppWidgetAlarm appWidgetAlarm = new AppWidgetAlarm(context.getApplicationContext());
            appWidgetAlarm.stopAlarm();
        }

        Intent serviceBG = new Intent(context.getApplicationContext(), WidgetBackgroundService.class);
        serviceBG.putExtra("SHUTDOWN", true);
        context.getApplicationContext().startService(serviceBG);
        context.getApplicationContext().stopService(serviceBG);
    }

    private void restartAll(Context context) {
        Intent serviceBG = new Intent(context.getApplicationContext(), WidgetBackgroundService.class);
        context.getApplicationContext().startService(serviceBG);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            scheduleJob(context);
        } else {
            AppWidgetAlarm appWidgetAlarm = new AppWidgetAlarm(context.getApplicationContext());
            appWidgetAlarm.startAlarm();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void scheduleJob(Context context) {
        ComponentName serviceComponent = new ComponentName(context.getPackageName(), RepeatingJob.class.getName());
        JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent);
        builder.setPersisted(true);
        builder.setPeriodic(600000);
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        int jobResult = jobScheduler.schedule(builder.build());
        if (jobResult == JobScheduler.RESULT_SUCCESS){
        }
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
