package asia.fivejuly.securepreferences.sample;

import android.app.Application;

import asia.fivejuly.securepreferences.SecurePreferences;

/**
 * Created by haipq on 10/30/17.
 */

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SecurePreferences.init(this);
    }

}
