package nonoobs.cryptopricewidgets.service;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.widget.RemoteViews;

import com.rvalerio.fgchecker.AppChecker;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
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
    private boolean mPaused = false;
    private boolean mStop = false;
    private Service mService;

    private HashSet<String> mHomeList;

    public CryptoPriceServiceThread(Service s) {
        mService = s;
    }

    public synchronized void pause() {
        if (!mPaused) {
            mPaused = true;
            notify();
        }
    }

    public synchronized void unpause() {
        mPaused = false;
        notify();
    }

    public synchronized void kill() {
        mStop = true;
        notify();
    }

    public void run() {
        refreshHomeList();

        while (!mStop) {
            updateGDAXPrice(mGdaxPrice);
            updateWidget(mGdaxPrice);

            CryptoAppWidgetLogger.info("Price updated: " + mGdaxPrice.price);

            synchronized (this) {
                try {
                    wait(isHomeShowing() ? 1000 : 5000);
                } catch (InterruptedException e) {
                }

                while (mPaused && !mStop) {
                    CryptoAppWidgetLogger.info("CryptoPriceServiceThread paused");
                    try {
                        wait();
                    } catch (InterruptedException e) {
                    }
                    CryptoAppWidgetLogger.info("CryptoPriceServiceThread unpaused");
                    refreshHomeList();
                }
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

    private static final double INVALID_VALUE = -1.0;

    class PriceData {
        double price = INVALID_VALUE;
        double lastPrice = INVALID_VALUE;
        boolean unknown = true;
    }

    private PriceData mGdaxPrice = new PriceData();

    private void updateGDAXPrice(PriceData priceData) {
        double price;

        priceData.unknown = true;

        try {
            URL url = new URL("https://api.gdax.com/products/BTC-USD/book");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(1000);
            connection.setReadTimeout(1000);
            connection.connect();

            BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            JSONObject obj = new JSONObject(rd.readLine());

            //{"sequence":2942300448,"bids":[["1624.98","1.13126808",5]],"asks":[["1624.99","11.26165044",1]]}

            //JSONArray bids = (JSONArray) obj.get("bids");
            JSONArray asks = (JSONArray) obj.get("asks");

            price = Double.parseDouble((String) ((JSONArray) asks.get(0)).get(0));

            if (priceData.price != price) {
                priceData.lastPrice = priceData.price;
                priceData.price = price;
            }

            priceData.unknown = false;
        } catch (Exception e) {
            CryptoAppWidgetLogger.info(e.toString());
        }
    }

    private void updateWidget(PriceData priceData) {
        RemoteViews views = new RemoteViews(mService.getPackageName(), R.layout.crypto_widget);

        String priceText = priceData.price == INVALID_VALUE ? "" : String.format("$%.2f", priceData.price);
        int color = priceData.lastPrice < priceData.price ? 0xFF7EEB63 : 0xFFEB6439;

        if (priceData.unknown) {
            color = color & 0x80FFFFFF;
        }

        views.setTextViewText(R.id.appwidget_text, priceText);
        views.setTextColor(R.id.appwidget_text, color);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mService.getApplicationContext());
        appWidgetManager.updateAppWidget(new ComponentName(mService.getApplicationContext(), CryptoAppWidgetProvider.class), views);
    }
}
