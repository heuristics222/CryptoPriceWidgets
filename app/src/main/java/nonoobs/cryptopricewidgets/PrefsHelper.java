package nonoobs.cryptopricewidgets;

import android.content.SharedPreferences;

import java.util.Map;

/**
 * Created by Doug on 2017-05-05.
 */

public class PrefsHelper
{

    public static void serializeWidgetSettingsToPrefs(SharedPreferences prefs, WidgetSettings settings)
    {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(settings.getID() + "source", settings.getSource());
        editor.putString(settings.getID() + "product", settings.getProduct());
        editor.apply();
    }

    public static WidgetSettings serializeWidgetSettingsFromPrefs(SharedPreferences prefs, int widgetID)
    {
        WidgetSettings settings = new WidgetSettings(widgetID);
        settings.setSource(prefs.getInt(widgetID + "source", 0));
        settings.setProduct(prefs.getString(widgetID + "product", ""));
        return settings;
    }

    public static void removeAll(SharedPreferences prefs)
    {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }

    public static void remove(SharedPreferences prefs, int widgetID)
    {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(widgetID + "source");
        editor.remove(widgetID + "product");
        editor.apply();
    }
}
