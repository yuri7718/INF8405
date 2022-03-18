package com.android.bluetoothscanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;


import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.Set;

import model.BluetoothScanner;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ACCESS_COARSE_LOCATION = 2;
    private Button darkModeToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button mPlayButton = findViewById(R.id.main_activity_play_button);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestStoragePermission();
        }

        // connecting play button to puzzle activity
        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // start puzzle activity
                Intent puzzleActivityIntent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(puzzleActivityIntent);
            }
        });

    }
    private void requestStoragePermission() {
        new AlertDialog.Builder(this)
                .setTitle("Permission needed")
                .setMessage("This permission is needed")
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[] {Manifest.permission.ACCESS_COARSE_LOCATION},
                                REQUEST_ACCESS_COARSE_LOCATION);
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_ACCESS_COARSE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission GRANTED", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }

     @Override
    protected void onCreate( Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
  
        darkModeToggle  = findViewById(R.id.darkModeToggle);
  
        darkModeToggle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    AppCompatDelegate.setDefaultNightMode( AppCompatDelegate.MODE_NIGHT_YES);
                }
            });
    }
}

}