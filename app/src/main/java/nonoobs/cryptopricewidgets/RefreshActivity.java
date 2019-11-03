package nonoobs.cryptopricewidgets;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import nonoobs.cryptopricewidgets.service.CryptoPriceService;

public class RefreshActivity extends Activity {
    public RefreshActivity() {
        super();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CryptoAppWidgetLogger.info("RefreshActivity.onCreate");

        Intent intent = new Intent(getApplicationContext(), RefreshActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);


        AlarmManager alarmManager = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);

        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

        alarmManager.setExact(AlarmManager.RTC, System.currentTimeMillis() + 1000, pi);

        getApplicationContext().startService(new Intent(getApplicationContext(), CryptoPriceService.class));
        finish();
    }
}
