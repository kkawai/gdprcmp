package org.gdprcmplib;

import android.util.Log;

final class MLog {

    private MLog() {
    }

    private static boolean isEnabled() {
        return Config.IS_LOGGING_ENABLED;
    }

    public static void i(String tag, Object... vals) {
        if (isEnabled()) {
            Log.i(tag, buildString(vals));
        }
    }

    public static void w(String tag, Object... vals) {
        if (isEnabled()) {
            Log.w(tag, buildString(vals));
        }
    }

    public static void d(String tag, Object... vals) {
        if (isEnabled()) {
            Log.d(tag, buildString(vals));
        }
    }

    public static void e(String tag, Object... vals) {
        if (isEnabled()) {
            Log.e(tag, buildString(vals));
        }
    }

    public static void e(String tag, String log, Throwable t) {
        if (isEnabled()) {
            Log.e(tag,log, t);
        }
    }

    private static String buildString(Object... strings) {
        final StringBuilder sb = new StringBuilder();
        for (Object s : strings) {
            sb.append(s);
        }
        return sb.toString();
    }

}
