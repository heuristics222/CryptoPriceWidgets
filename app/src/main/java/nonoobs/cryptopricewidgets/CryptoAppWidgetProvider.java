package nonoobs.cryptopricewidgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

import nonoobs.cryptopricewidgets.service.CryptoPriceService;

/**
 * Created by Doug on 2017-05-04.
 */

public class CryptoAppWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        CryptoAppWidgetLogger.info("CryptoAppWidgetProvider.onUpdate called");

        Intent serviceIntent = new Intent(context, CryptoPriceService.class);
        context.startForegroundService(serviceIntent);

        Intent intent = new Intent(context, getClass());
        intent.setAction("Click");
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.crypto_widget);
        views.setOnClickPendingIntent(R.id.appwidget_container, PendingIntent.getBroadcast(context, 0, intent, 0));
        appWidgetManager.updateAppWidget(new ComponentName(context, CryptoAppWidgetProvider.class), views);

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        CryptoAppWidgetLogger.info("CryptoAppWidgetProvider.onReceive called");

        if ("Click".equals(intent.getAction())) {
            Intent serviceIntent = new Intent(context, CryptoPriceService.class);
            context.startForegroundService(serviceIntent);
        }

        super.onReceive(context, intent);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        CryptoAppWidgetLogger.info("CryptoAppWidgetProvider.onAppWidgetOptionsChanged called");
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        CryptoAppWidgetLogger.info("CryptoAppWidgetProvider.onDeleted called");
        for (int x : appWidgetIds) {
            PrefsHelper.remove(PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext()), x);
        }

        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        CryptoAppWidgetLogger.info("CryptoAppWidgetProvider.onEnabled called");
        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {
        CryptoAppWidgetLogger.info("CryptoAppWidgetProvider.onDisabled called");
        PrefsHelper.removeAll(PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext()));
        Intent serviceIntent = new Intent(context, CryptoPriceService.class);
        context.stopService(serviceIntent);
        super.onDisabled(context);
    }
}
