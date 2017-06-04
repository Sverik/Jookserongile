package com.photonorbit.jookserongile;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class FetchTimesTask extends AsyncTask<String, Integer, Long> {
    private Context context;
    private DataUtil.DoneCallback doneLoading;

    public FetchTimesTask(Context context, DataUtil.DoneCallback doneLoading) {
        this.context = context;
        this.doneLoading = doneLoading;
    }

    @Override
    protected Long doInBackground(String... params) {
        String table = params[0];
        Log.i("async", "fetching for " + table);
        long time = System.currentTimeMillis();
        List<DataUtil.Row> rows = new ArrayList<>(5);
        rows.add(new DataUtil.Row(table, timeToString(time), Long.toString(time)));
        time += 1 * 60 * 1000;
        rows.add(new DataUtil.Row(table, timeToString(time), Long.toString(time)));
        time += 3 * 60 * 1000;
        rows.add(new DataUtil.Row(table, timeToString(time), Long.toString(time)));
        time += 10 * 60 * 1000;
        rows.add(new DataUtil.Row(table, timeToString(time), Long.toString(time)));
        time += 6 * 60 * 1000;
        rows.add(new DataUtil.Row(table, timeToString(time), Long.toString(time)));

        DataUtil.setRows(context, table, rows);
        return null;
    }

    protected String timeToString(long time) {
        Calendar c = GregorianCalendar.getInstance();
        c.setTimeInMillis(time);
        return c.get(Calendar.HOUR) + ":" + String.format("%02d", c.get(Calendar.MINUTE));
    }

    @Override
    protected void onPostExecute(Long aLong) {
        doneLoading.done();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
    }
}
