package com.photonorbit.jookserongile;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
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
        int minuteOffset = table.hashCode() % 10;
        Log.i("async", "minuteOffset = " + minuteOffset);
        long start = time / (1000 * 60 * 60);
        start *= 1000 * 60 * 60;
        Log.i("async", "time=" + time + ", start=" + start);
        List<DataUtil.Row> rows = new ArrayList<>(21);
        long dataTime = start + minuteOffset * 1000 * 60;
        for (int i = 0 ; i < 21 ; i++) {
            rows.add(new DataUtil.Row(table, timeToString(dataTime), Long.toString(dataTime), dataTime));
            dataTime += 6 * 60 * 1000;
        }

        DataUtil.setRows(context, table, rows);
        return null;
    }

    public static String timeToString(long time) {
        Calendar c = GregorianCalendar.getInstance();
        c.setTimeInMillis(time);
        return c.get(Calendar.HOUR_OF_DAY) + ":" + String.format("%02d", c.get(Calendar.MINUTE));
    }

    @Override
    protected void onPostExecute(Long aLong) {
        doneLoading.done();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
    }
}
