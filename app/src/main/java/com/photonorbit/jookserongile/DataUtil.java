package com.photonorbit.jookserongile;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

public class DataUtil {
    private static final String PREFS = "Times_";
    private static final String LINE = "_LINE";
    private static final String TIME = "_TIME";
    private static final String TIMESTAMP = "_TIMESTAMP";

    public static class Row {
        public String line;
        public String time;
        public String timestamp;

        public Row(String line, String time, String timestamp) {
            this.line = line;
            this.time = time;
            this.timestamp = timestamp;
        }

        @Override
        public String toString() {
            return "Row{" +
                    "line='" + line + '\'' +
                    ", time='" + time + '\'' +
                    ", timestamp='" + timestamp + '\'' +
                    '}';
        }
    }

    public static interface DoneCallback {
        void done();
    }

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
                // startShowingFrom is before timestamp, this has to be shown
                break;
            }
        }
        while (i >= 0 && res.size() < count) {
            String line = prefs.getString(i + LINE, null);
            String time = prefs.getString(i + TIME, null);
            if (line == null) {
                needsFetching = true;
                break;
            }
            Row row = new Row(line, time, null);
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
            editor.putString(i + TIMESTAMP, row.timestamp);
            i++;
        }
        editor.commit();
    }
}
