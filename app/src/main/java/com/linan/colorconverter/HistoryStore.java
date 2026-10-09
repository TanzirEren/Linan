package com.linan.colorconverter;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

/** Keeps the last converted colors in SharedPreferences. */
public final class HistoryStore {

    private static final String PREFS = "linan_prefs";
    private static final String KEY = "history";
    private static final int MAX = 20;

    private HistoryStore() {}

    public static List<Integer> getAll(Context c) {
        List<Integer> out = new ArrayList<Integer>();
        String raw = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, "");
        if (raw.length() == 0) return out;
        String[] parts = raw.split(",");
        for (int i = 0; i < parts.length; i++) {
            try {
                out.add((int) Long.parseLong(parts[i]));
            } catch (NumberFormatException ignored) {
            }
        }
        return out;
    }

    public static void add(Context c, int argb) {
        List<Integer> list = getAll(c);
        list.remove(Integer.valueOf(argb));
        list.add(0, argb);
        while (list.size() > MAX) list.remove(list.size() - 1);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(list.get(i).intValue() & 0xFFFFFFFFL);
        }
        SharedPreferences sp = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        sp.edit().putString(KEY, sb.toString()).apply();
    }

    public static void clear(Context c) {
        c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(KEY).apply();
    }
}
