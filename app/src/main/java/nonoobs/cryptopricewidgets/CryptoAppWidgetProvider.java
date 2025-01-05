package nonoobs.cryptopricewidgets;

import static android.content.Context.POWER_SERVICE;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.StrictMode;
import android.provider.Settings;
import android.widget.RemoteViews;

import androidx.datastore.rxjava2.RxDataStore;
import androidx.datastore.rxjava2.RxDataStoreBuilder;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import io.reactivex.Single;
import nonoobs.cryptopricewidgets.model.PriceData;

/**
 * Created by Doug on 2017-05-04.
 */
public class CryptoAppWidgetProvider extends AppWidgetProvider {

    public static final String UPDATE_EVENT = "CryptoAppWidgetProvider.UPDATE_EVENT";

    private static RxDataStore<PriceData> dataStoreRX;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        CryptoAppWidgetLogger.info("CryptoAppWidgetProvider.onUpdate called");

        updateWidget(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        CryptoAppWidgetLogger.info("CryptoAppWidgetProvider.onReceive called: " + intent.getAction());

        if (intent.getAction().equals(UPDATE_EVENT)) {
            updateWidget(context);
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

        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        CryptoAppWidgetLogger.info("CryptoAppWidgetProvider.onEnabled called");

        WorkManager.getInstance(context).enqueueUniquePeriodicWork("CryptoAppWidgetProvider.UPDATER", ExistingPeriodicWorkPolicy.KEEP,
                new PeriodicWorkRequest.Builder(WidgetUpdateWorker.class, 15, TimeUnit.MINUTES).build());

        Intent intent = new Intent();
        String packageName = context.getPackageName();
        PowerManager pm = (PowerManager) context.getSystemService(POWER_SERVICE);
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + packageName));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }

        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {
        CryptoAppWidgetLogger.info("CryptoAppWidgetProvider.onDisabled called");
        super.onDisabled(context);
    }

    static JSONObject getPriceStats() throws IOException, JSONException {
        URL url = new URL("https://api.exchange.coinbase.com/products/BTC-USD/stats");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setConnectTimeout(1000);
        connection.setReadTimeout(1000);
        connection.connect();

        try (BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            // {"open":"76925.09","high":"81534.28","low":"76700","last":"80516.15","volume":"16300.57951062","volume_30day":"348954.6390969","rfq_volume_24hour":"45.752852","rfq_volume_30day":"1505.235094"}
            return new JSONObject(rd.readLine());
        }
    }

    static void updateWidget(Context context) {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
        System.setProperty("java.net.preferIPv4Stack", "true");

        if (dataStoreRX == null) {
            dataStoreRX = new RxDataStoreBuilder<>(context, "settings.db", new PriceDataSerializer()).build();
        }

        Intent updateWidgetIntent = new Intent(context, CryptoAppWidgetProvider.class);
        updateWidgetIntent.setAction(UPDATE_EVENT);

        dataStoreRX.data().map(priceData -> {
                    CryptoAppWidgetLogger.info(String.format("PriceData from prefs: %d, %d", priceData.getPrice(), priceData.getPrevPrice()));
                    return 1.0;
                });

        try {
            PriceData data = dataStoreRX.data().blockingFirst();
            CryptoAppWidgetLogger.info(String.format("PriceData from prefs: %d, %d", data.getPrice(), data.getPrevPrice()));
        } catch (Throwable e) {
            CryptoAppWidgetLogger.info("Failed to get prefs...");
        }


        boolean unknown = false;
        double price = 0.0;
        double prevPrice = 0.0;

        try {
            JSONObject priceObj = getPriceStats();
            final double prevPrice1 = priceObj.getDouble("open");
            final double price1 = priceObj.getDouble("last");

            dataStoreRX.updateDataAsync(
                    currentPriceData -> Single.just(
                            currentPriceData.toBuilder()
                                    .setPrice(price1)
                                    .setPrevPrice(prevPrice1)
                                    .build()
                    )
            ).blockingGet();
            price = price1;
            prevPrice = prevPrice1;
        } catch (Throwable e) {
            unknown = true;
        }

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.crypto_widget);

        String priceText = unknown ? "" : String.format(price >= 100000 ? "$%.0f" : "$%.2f", price);

        CryptoAppWidgetLogger.info("Updated price: " + priceText);

        int color = prevPrice < price ? 0xFF7EEB63 : 0xFFEB6439;

        if (unknown) {
            color = color & 0x80FFFFFF;
        }

        views.setInt(R.id.appwidget_container, "setBackgroundResource",
                prevPrice < price ? R.drawable.rounded_rect_green : R.drawable.rounded_rect_red);
        views.setTextViewText(R.id.appwidget_price, priceText);
        views.setTextColor(R.id.appwidget_price, color);
        views.setTextViewText(R.id.appwidget_conversion, "USD/BTC");
        //views.setTextColor(R.id.appwidget_conversion, color);
        views.setOnClickPendingIntent(R.id.appwidget_container,
                PendingIntent.getBroadcast(context, 0, updateWidgetIntent, PendingIntent.FLAG_IMMUTABLE));

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(new ComponentName(context, CryptoAppWidgetProvider.class), views);
    }
}
