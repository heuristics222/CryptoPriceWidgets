package nonoobs.cryptopricewidgets;



import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class WidgetUpdateWorker extends Worker {

    public WidgetUpdateWorker(Context context, WorkerParameters params) { super(context, params); }

    @NonNull
    @Override
    public Result doWork() {
        CryptoAppWidgetLogger.info("WidgetUpdateWorker.doWork");

        CryptoAppWidgetProvider.updateWidget(getApplicationContext());

        return Result.success();
    }
}
