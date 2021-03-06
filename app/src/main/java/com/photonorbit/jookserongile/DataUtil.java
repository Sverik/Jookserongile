package com.photonorbit.jookserongile;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DataUtil {
    private static final String PREFS = "Times_";
    private static final String LINE = "_LINE";
    private static final String TIME = "_TIME";
    private static final String TIMESTAMP = "_TIMESTAMP";

    public static class Row {
        public String line;
        public String time;
        public String timestampString;
        public long timestamp;

        public Row(String line, String time, String timestampString, long timestamp) {
            this.line = line;
            this.time = time;
            this.timestampString = timestampString;
            this.timestamp = timestamp;
        }

        @Override
        public String toString() {
            return "Row{" +
                    "line='" + line + '\'' +
                    ", time='" + time + '\'' +
                    ", timestampString='" + timestampString + '\'' +
                    '}';
        }
    }

    public interface DoneCallback {
        void done();
    }

    protected static String liveFromTime = null;
    protected static AtomicInteger liveUpdateCount = new AtomicInteger(0);

    public static List<Row> getNextRows(Context context, String table, long currentTime, int count, DoneCallback doneLoading) {
        List<Row> res = new ArrayList<>(count);
        SharedPreferences prefs = context.getSharedPreferences(PREFS + table, Context.MODE_PRIVATE);
        boolean needsFetching = false;
        int i = 0;
        String startShowingFrom = Long.toString(currentTime);
        for ( ; ; i++) {
            String timestamp = prefs.getString(i + TIMESTAMP, null);
            if (timestamp == null) {
                needsFetching = true;
                i = -1;
                break;
            }
            if (startShowingFrom.compareTo(timestamp) < 0) {
                // startShowingFrom is before timestampString, this has to be shown
                break;
            }
        }
        if (liveFromTime == null) {
            liveFromTime = FetchTimesTask.timeToString(currentTime);
        }
        liveUpdateCount.incrementAndGet();
        while (i >= 0 && res.size() < count) {
            String line = prefs.getString(i + LINE, null);
            String time = prefs.getString(i + TIME, null);
            String timestampString = prefs.getString(i + TIMESTAMP, null);
            if (line == null) {
                needsFetching = true;
                break;
            }
            line += "[" + liveFromTime + ";" + liveUpdateCount.get() + "]";
            Row row = new Row(line, time, timestampString, Long.parseLong(timestampString));
            res.add(row);
            i++;
        }
        if (needsFetching) {
            new FetchTimesTask(context, doneLoading).execute(table);
        }

        return res;
    }

    public static void setRows(Context context, String table, List<Row> rows) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS + table, Context.MODE_PRIVATE).edit();
        editor.clear();
        int i = 0;
        for (Row row : rows) {
            editor.putString(i + LINE, row.line);
            editor.putString(i + TIME, row.time);
            editor.putString(i + TIMESTAMP, row.timestampString);
            i++;
        }
        editor.commit();
    }
}
