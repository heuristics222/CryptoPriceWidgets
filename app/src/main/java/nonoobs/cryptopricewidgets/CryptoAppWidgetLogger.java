package nonoobs.cryptopricewidgets;

import android.os.Build;
import android.util.Log;

/**
 * Created by Doug on 2017-05-13.
 */

public class CryptoAppWidgetLogger {
    private static final String TAG = "CryptoWidget";

    private static final LogLevel LOG_LEVEL;

    private enum LogLevel {
        ERROR,
        WARN,
        INFO,
    }

    static {
        if ("google_sdk".equals(Build.PRODUCT) || "sdk".equals(Build.PRODUCT)) {
            LOG_LEVEL = LogLevel.INFO;
        } else {
            LOG_LEVEL = LogLevel.ERROR;
        }
    }

    public static void error(String msg) {
        Log.d(TAG, msg);
    }

    public static void warn(String msg) {
        if (LOG_LEVEL.ordinal() >= LogLevel.WARN.ordinal()) {
            Log.d(TAG, msg);
        }
    }

    public static void info(String msg) {
        if (LOG_LEVEL.ordinal() >= LogLevel.INFO.ordinal()) {
            Log.d(TAG, msg);
        }
    }
}
