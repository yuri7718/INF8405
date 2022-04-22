package com.android.bluetoothscanner;

import static language.LocaleHelper.updateLanguage;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity{

    private static final int REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int TIME_OUT = 3000; // wait 3s before showing the main view of the application

    private static final String SHARED_PREFERENCES = "sharedPrefs";
    private static final String DARK_MODE = "isDarkModeOn";
    private static final String LANGUAGE = "language";

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);

        // get theme, default is false
        boolean isDarkModeOn = sharedPreferences.getBoolean(DARK_MODE, false);
        if (isDarkModeOn) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        // get language code, default is en
        String languageCode = sharedPreferences.getString(LANGUAGE, "en");
        updateLanguage(this, languageCode);

        setContentView(R.layout.activity_main);

        // verify permissions
        if (!checkCameraPermission()) {
            requestCameraPermission();
        }
        if (!checkLocationPermission()) {
            requestLocationPermission();
        } else {
            startMapsActivity();
        }
    }

    /**
     * Verify camera and write external storage permission
     */
    private boolean checkCameraPermission() {
        boolean res1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean res2 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        return res1 && res2;
    }

    /**
     * Request camera and write external storage permission
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestCameraPermission() {
        requestPermissions(new String[]{ Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
    }

    /**
     * Verify location permission
     */
    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Show dialog to request location permission
     */
    private void requestLocationPermission() {
        new AlertDialog.Builder(this)
            .setTitle("Permission needed")
            .setMessage("This permission is needed")
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                        new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ACCESS_FINE_LOCATION);
                }
            })
            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    MainActivity.this.finishAffinity();
                }
            }).create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission GRANTED", Toast.LENGTH_SHORT).show();
                startMapsActivity();
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
                MainActivity.this.finishAffinity();
            }
        }
    }

    /**
     * Start MapsActivity
     */
    private void startMapsActivity() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent mapsActivityIntent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(mapsActivityIntent);
                finish();
            }
        }, TIME_OUT);
    }

}