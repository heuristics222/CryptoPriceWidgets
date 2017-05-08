package nonoobs.cryptopricewidgets;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.RemoteViews;

import nonoobs.cryptopricewidgets.service.CryptoPriceService;

/**
 * Created by Doug on 2017-05-04.
 */

public class CryptoAppWidgetConfigure extends Activity
{
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    public CryptoAppWidgetConfigure()
    {
        super();
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setResult(RESULT_CANCELED);

        setContentView(R.layout.crypto_widget_configure);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null)
        {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID)
        {
            finish();
        }

        findViewById(R.id.button).setOnClickListener(mOnClickListener);
    }

    View.OnClickListener mOnClickListener = new View.OnClickListener()
    {
        public void onClick(View v)
        {
            RemoteViews views = new RemoteViews(getPackageName(), R.layout.crypto_widget);
            //views.setTextViewText(R.id.appwidget_text, CryptoAppWidgetProvider.getBTCValue());
            AppWidgetManager.getInstance(CryptoAppWidgetConfigure.this).updateAppWidget(mAppWidgetId, views);

            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);

            Intent serviceIntent = new Intent(getApplicationContext(), CryptoPriceService.class);
            getApplicationContext().startService(serviceIntent);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

            WidgetSettings settings = new WidgetSettings(mAppWidgetId);
            settings.setSource(WidgetSettings.SOURCE_GDAX);
            settings.setProduct("BTC-USD");
            PrefsHelper.serializeWidgetSettingsToPrefs(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()), settings);

            finish();
        }
    };
}
