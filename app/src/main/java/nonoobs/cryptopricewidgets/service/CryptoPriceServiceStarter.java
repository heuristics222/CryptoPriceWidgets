package nonoobs.cryptopricewidgets.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import nonoobs.cryptopricewidgets.CryptoAppWidgetLogger;

/**
 * Created by Doug on 2017-05-13.
 */

public class CryptoPriceServiceStarter extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case Intent.ACTION_MY_PACKAGE_REPLACED:
                CryptoAppWidgetLogger.info("CryptoPriceServiceStarter received ACTION_MY_PACKAGE_REPLACED");
                context.startService(new Intent(context, CryptoPriceService.class));
                break;
        }
    }
}
