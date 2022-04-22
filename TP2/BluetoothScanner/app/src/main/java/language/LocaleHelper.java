package language;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;

import com.android.bluetoothscanner.MapsActivity;

import java.util.Locale;

public class LocaleHelper {

    public static void updateLanguage(Context context, String languageCode) {
        languageCode = languageCode.toLowerCase();

        // update language
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }
}
