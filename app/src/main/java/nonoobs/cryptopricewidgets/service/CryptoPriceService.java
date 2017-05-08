package nonoobs.cryptopricewidgets.service;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

/**
 * Created by Doug on 2017-05-05.
 */

public class CryptoPriceService extends Service
{
    CryptoPriceServiceThread mThread = new CryptoPriceServiceThread(this);
    CryptoPriceReceiver mReceiver = new CryptoPriceReceiver(this);

    @Override
    public void onCreate()
    {
        mThread.start();

        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mReceiver, filter);
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    public void handleScreenOff()
    {
        mThread.pause();
    }

    public void handleScreenOn()
    {
        mThread.unpause();
    }
}
