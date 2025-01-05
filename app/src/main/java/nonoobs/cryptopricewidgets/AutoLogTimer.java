package nonoobs.cryptopricewidgets;

import java.io.Closeable;

/**
 * Created by Doug on 2017-12-01.
 */

public class AutoLogTimer implements Closeable {
    private final long mMillis;
    private final String mMessage;

    public AutoLogTimer(String message) {
        mMillis = System.currentTimeMillis();
        mMessage = message;
    }

    public void close() {
        CryptoAppWidgetLogger.info(mMessage + ": "  + (System.currentTimeMillis() - mMillis));
    }
}
