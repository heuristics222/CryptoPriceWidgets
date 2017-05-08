package nonoobs.cryptopricewidgets.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Doug on 2017-05-05.
 */

public class CryptoPriceReceiver extends BroadcastReceiver
{
    CryptoPriceService mService;

    public CryptoPriceReceiver(CryptoPriceService service)
    {
        mService = service;
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        switch (intent.getAction())
        {
            case Intent.ACTION_SCREEN_OFF:
                mService.handleScreenOff();
                break;
            case Intent.ACTION_SCREEN_ON:
                mService.handleScreenOn();
                break;
        }
    }
}
