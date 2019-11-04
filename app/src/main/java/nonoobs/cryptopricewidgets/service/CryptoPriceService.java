package nonoobs.cryptopricewidgets.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Icon;
import android.os.IBinder;
import android.widget.RemoteViews;

import nonoobs.cryptopricewidgets.CryptoAppWidgetLogger;
import nonoobs.cryptopricewidgets.CryptoAppWidgetProvider;
import nonoobs.cryptopricewidgets.R;

/**
 * Created by Doug on 2017-05-05.
 */

public class CryptoPriceService extends Service {
    CryptoPriceServiceThread mThread = new CryptoPriceServiceThread(this);
    CryptoPriceReceiver mReceiver = new CryptoPriceReceiver(this);

    @Override
    public void onCreate() {
        CryptoAppWidgetLogger.info("CryptoPriceService.onCreate() called");
        mThread.start();

        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        CryptoAppWidgetLogger.info("CryptoPriceService.onStartCommand() called");
        if ("STOP".equals(intent.getAction())) {
            stopForeground(Service.STOP_FOREGROUND_REMOVE);
            stopSelf();
        } else {
            Intent notificationIntent = new Intent(getApplicationContext(), CryptoPriceService.class);
            notificationIntent.setAction("STOP");
            PendingIntent pi = PendingIntent.getService(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

            startForeground(12345, new Notification.Builder(getApplicationContext(), "CryptoUpdater")
                    .setSmallIcon(Icon.createWithResource(getApplicationContext(), R.drawable.ic_trending_up_24px))
                    .setContentTitle("Tap to stop widget updates, restart by tapping the widget.")
                    .setContentText("")
                    .setContentIntent(pi)
                    .setAutoCancel(true)
                    .build());
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        CryptoAppWidgetLogger.info("CryptoPriceService.onDestroy() called");
        unregisterReceiver(mReceiver);
        mThread.kill();

        RemoteViews views = new RemoteViews(getPackageName(), R.layout.crypto_widget);
        views.setInt(R.id.appwidget_container, "setBackgroundResource", R.drawable.rounded_rect);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
        appWidgetManager.updateAppWidget(new ComponentName(getApplicationContext(), CryptoAppWidgetProvider.class), views);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void handleScreenOff() {
        mThread.pause();
    }

    public void handleScreenOn() {
        mThread.unpause();
    }
}
