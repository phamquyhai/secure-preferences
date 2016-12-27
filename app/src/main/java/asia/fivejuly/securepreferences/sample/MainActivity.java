package asia.fivejuly.securepreferences.sample;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;


import asia.fivejuly.securepreferences.SecurePreferences;

/**
 * Created by haipq on 12/27/16.
 */

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        SharedPreferences shareConceal = new SecurePreferences.Builder(MainActivity.this)
                .password("password")
                .filename("settings")
                .build();

        shareConceal.edit().putString("test","Hello").apply();

        Log.d(getLocalClassName()," test - > " + shareConceal.getString("test",""));

    }

}
