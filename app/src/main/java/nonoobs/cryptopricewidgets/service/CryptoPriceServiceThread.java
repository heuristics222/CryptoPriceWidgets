package nonoobs.cryptopricewidgets.service;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.util.Log;
import android.widget.RemoteViews;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import nonoobs.cryptopricewidgets.CryptoAppWidgetProvider;
import nonoobs.cryptopricewidgets.R;

/**
 * Created by Doug on 2017-05-06.
 */

public class CryptoPriceServiceThread extends Thread
{
    private boolean mPaused = false;
    private Service mService;


    public CryptoPriceServiceThread(Service s)
    {
        mService = s;
    }

    public synchronized void pause()
    {
        mPaused = true;
    }

    public synchronized void unpause()
    {
        mPaused = false;
        notify();
    }

    public void run()
    {
        while (true)
        {
            synchronized (this)
            {
                while (mPaused)
                {
                    try { wait(); } catch (InterruptedException e) {}
                }
            }

            try
            {
                String price;

                URL url = new URL("https://api.gdax.com/products/BTC-USD/book");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");
                connection.setConnectTimeout(1000);
                connection.setReadTimeout(1000);
                connection.connect();

                BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                JSONObject obj = new JSONObject(rd.readLine());

                //{"sequence":2942300448,"bids":[["1624.98","1.13126808",5]],"asks":[["1624.99","11.26165044",1]]}

                JSONArray bids = (JSONArray) obj.get("bids");
                JSONArray asks = (JSONArray) obj.get("asks");

                price = (String) ((JSONArray) asks.get(0)).get(0);
                price = String.format("$%.2f", Double.parseDouble(price));

                RemoteViews views = new RemoteViews(mService.getPackageName(), R.layout.crypto_widget);
                views.setTextViewText(R.id.appwidget_text, price);
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mService.getApplicationContext());
                appWidgetManager.updateAppWidget(new ComponentName(mService.getApplicationContext(), CryptoAppWidgetProvider.class), views);

                Log.d("CryptoWidget", price);
            }
            catch (Exception e)
            {
                Log.d("CryptoWidget", e.toString());
            }

            try { Thread.sleep(1000); } catch (InterruptedException e) { }
        }
    }
}
