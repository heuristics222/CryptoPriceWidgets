package nonoobs.cryptopricewidgets.service;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.PowerManager;
import android.widget.RemoteViews;

import com.rvalerio.fgchecker.AppChecker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;

import nonoobs.cryptopricewidgets.AutoLogTimer;
import nonoobs.cryptopricewidgets.CryptoAppWidgetLogger;
import nonoobs.cryptopricewidgets.CryptoAppWidgetProvider;
import nonoobs.cryptopricewidgets.R;

/**
 * Created by Doug on 2017-05-06.
 */

public class CryptoPriceServiceThread extends Thread {
    private boolean mStop = false;
    private Service mService;

    private HashSet<String> mHomeList;

    double mOpenPrice = 0.0;
    long mLastOpenUpdated = 0L;

    double mCurrentPrice = -1.0;

    public CryptoPriceServiceThread(Service s) {
        mService = s;
    }

    public synchronized void kill() {
        mStop = true;
        notify();
    }

    public void run() {
        refreshHomeList();

        while (!mStop) {
            try {
                updateHistoricalData();
                updateGDAXPrice();
                updateWidget();

                CryptoAppWidgetLogger.info("Price updated: " + mCurrentPrice);

                synchronized (this) {
                    try {
                        wait(isHomeShowing() ? 1000 : 5000);
                    } catch (InterruptedException e) {
                    }

                    if (!((PowerManager) mService.getApplicationContext().getSystemService(Context.POWER_SERVICE)).isInteractive()) {
                        CryptoAppWidgetLogger.info("Stopping service");
                        mService.stopSelf();
                    }
                }
            } catch (Exception e) {
                CryptoAppWidgetLogger.info(e.toString());
            }
        }

        CryptoAppWidgetLogger.info("CryptoPriceServiceThread ending");
    }

    private void refreshHomeList() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        mHomeList = new HashSet<>();
        for (ResolveInfo info : mService.getPackageManager().queryIntentActivities(intent, 0)) {
            mHomeList.add(info.activityInfo.packageName);
        }
    }

    private boolean isHomeShowing() {
        try (AutoLogTimer x = new AutoLogTimer("isHomeShowing")) {
            String p = new AppChecker().getForegroundApp(mService.getApplicationContext());
            return mHomeList.contains(p);
        }
    }

    private JSONObject get(String url) throws Exception {
        URL u = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) u.openConnection();

        connection.setRequestMethod("GET");
        connection.setConnectTimeout(1000);

        connection.setReadTimeout(1000);
        connection.connect();

        BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
        return new JSONObject(rd.readLine());
    }

    private void updateHistoricalData() throws Exception {
        long time = System.currentTimeMillis();
        if (time - mLastOpenUpdated > 60*60*1000) {
            JSONObject obj = get("https://api.pro.coinbase.com/products/BTC-USD/stats");
            mOpenPrice = Double.parseDouble((String) obj.get("open"));
            mLastOpenUpdated = time;
            CryptoAppWidgetLogger.info("Updating historical price: " + mOpenPrice);
        }
    }

    private void updateGDAXPrice() throws Exception {
        JSONObject obj = get("https://api.pro.coinbase.com/products/BTC-USD/book");
        JSONArray asks = (JSONArray) obj.get("asks");
        mCurrentPrice = Double.parseDouble((String) ((JSONArray) asks.get(0)).get(0));
    }

    private void updateWidget() {
        RemoteViews views = new RemoteViews(mService.getPackageName(), R.layout.crypto_widget);

        String priceText = mCurrentPrice < 0.0 ? "" : String.format("$%.2f", mCurrentPrice);
        int color = mOpenPrice < mCurrentPrice ? 0xFF7EEB63 : 0xFFEB6439;

        if (mCurrentPrice < 0.0) {
            color = color & 0x80FFFFFF;
        }

        views.setInt(R.id.appwidget_container, "setBackgroundResource", mOpenPrice < mCurrentPrice ?
                R.drawable.rounded_rect_green : R.drawable.rounded_rect_red);

        views.setTextViewText(R.id.appwidget_price, priceText);
        views.setTextColor(R.id.appwidget_price, color);
        views.setTextViewText(R.id.appwidget_conversion, "USD/BTC");
        //views.setTextColor(R.id.appwidget_conversion, color);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mService.getApplicationContext());
        appWidgetManager.updateAppWidget(new ComponentName(mService.getApplicationContext(), CryptoAppWidgetProvider.class), views);
    }
}
